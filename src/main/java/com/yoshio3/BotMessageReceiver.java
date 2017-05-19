/*
 * Copyright 2017 Yoshio Terada
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yoshio3;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.yoshio3.rest.entities.bot.MessageFromBotFrameWork;
import com.yoshio3.rest.entities.bot.CommonMessageFromBotFramework;
import com.yoshio3.rest.entities.luis.ResponseFromLUIS;
import com.yoshio3.rest.entities.luis.childelements.Entity;
import com.yoshio3.services.AccessTokenForBotService;
import com.yoshio3.services.LUISService;
import com.yoshio3.services.BotService;
import com.yoshio3.services.TranslatorTextServices;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

/**
 * Bot MessageReceiver This class receive the message from Cliant Application
 * like Web, Skype, FaceBook, Slack and so on. After receviced the message, it
 * pass the message to the LUIS and caluculate the most possible pattern.
 *
 * @author Yoshio Terada
 */
@Path("message")
public class BotMessageReceiver {

    private final static Logger LOGGER = Logger.getLogger(BotMessageReceiver.class.getName());

    private final static String ENTRYPOINT_OF_JSR_NUMBER_SEARCH = "http://jsr-confim-msa.52.175.149.226.nip.io/JSR-Confirm-Service-MSA-1.0-SNAPSHOT/rest/jsr/";
    private final static String ENTRYPOINT_OF_JSR_AMBIGUOUS_SEARCH = "http://jsr-confim-msa.52.175.149.226.nip.io/JSR-Confirm-Service-MSA-1.0-SNAPSHOT/rest/jsr/search?name=";

    @Context
    private ResourceContext resourceContext;

    @Resource //(In Payara "Concurrency Utilities for Java EE" is supported as default)
    ManagedExecutorService managedExecsvc;

    /**
     * POST Action
     *
     * In order to handle the message from clients, this methos is the entry
     * pont of this class.
     *
     * @param message messages from the clients
     * @return {@code Response} You received the message, you have to send back
     * to the "ACCEPTED" response to the client. And concurrently, you need to
     * operate somethings.
     */
    @POST
    @Consumes("application/json")
    public Response post(CommonMessageFromBotFramework message) {
        managedExecsvc.submit(() -> invokeService(message));
        return Response.ok().status(Response.Status.ACCEPTED).build();
    }

    /**
     * POST Action
     *
     * In order to handle the message from clients, this methos is the entry
     * pont of this class.
     *
     * @param message messages from the clients
     * @return {@code Response} You received the message, you have to send back
     * to the "ACCEPTED" response to the client. And concurrently, you need to
     * operate somethings.
     */
    private void invokeService(MessageFromBotFrameWork requestMessage) {
        final String inputString = requestMessage.getText();
        LOGGER.log(Level.FINE, "Input Data from user : {0}", inputString);
        String action = requestMessage.getAction();

        // Note: You need to learn more about action.
        // I didn't implement all of actions.
        if (action == null) {
            LOGGER.log(Level.FINE, "ACTION NULL");
            //Following is the action of bot behavior.
            if (!requestMessage.getId().isEmpty()) {
                LOGGER.log(Level.FINE, "Request Message is not Empty");
                invokeLUISAndSendResponse(requestMessage, inputString);
            }
        } else if (action.equals("add")) {
            String message = "Welcome to My SkypeBot";
            sendMessageToBotFramework(requestMessage, message);
        } else {
            LOGGER.log(Level.FINE, "ACTION IS INVALID ? :{0}", requestMessage);
        }
    }

    /**
     * Invoke LUISService
     *
     * After received the message from clients, we need to analysis the message.
     *
     * @param requestMessage messages from the clients
     * @param inputMessage inputed message from User.
     */
    private void invokeLUISAndSendResponse(MessageFromBotFrameWork requestMessage, String inputMessage) {
        //Invoke LUIS Services
        LUISService luisService = new LUISService();
        Optional<ResponseFromLUIS> responseFromLUIS = luisService.getResponseFromLUIS(inputMessage);

        responseFromLUIS.ifPresent(luis -> { 
            String topIntent = luis.getTopScoringIntent().getIntent();
            LOGGER.log(Level.FINE, "TopIntent is : {0}", topIntent);
            // If I implment more, I need to write follows as Enumeration.
            switch (topIntent) {
                case "JSR 番号の詳細":
                    execForJSRSearch(requestMessage, luis);
                    break;
                case "JSRあいまい検索":
                    execForAmbiguousSearch(requestMessage, luis);
                    break;
                case "JSR-Translate":
                    translateEnglish(requestMessage, luis);
                    break;
                default:
                    execActionForNothing(requestMessage);
                    break;
            }
        });
    }

    /**
     * Get a JSR contntes from the JSR-Number
     *
     * It will be called when LUIS caluculate user would like to get specific
     * JSR information.
     *
     * @param requestMessage messages from the clients
     * @param luis Response object from LUIS.
     */
    private void execForJSRSearch(MessageFromBotFrameWork requestMessage, ResponseFromLUIS luis) {
        List<Entity> entities = Arrays.asList(luis.getEntities());
        Integer jsrNumber = getJSRNumber(entities);
        Response response = getJSRInfo(entities);

        if (isRequestSuccess(response)) {
            JSRResultJSONMapping result = response
                    .readEntity(JSRResultJSONMapping.class);

            sendMessageToBotFramework(requestMessage, "JSR " + jsrNumber + " の詳細は 「" + result.getDescription() + "」 です。<BR>"
                    + "https://jcp.org/en/jsr/detail?id=" + jsrNumber);
        } else {
            String error = response.readEntity(String.class);
            LOGGER.log(Level.SEVERE, error);
            sendMessageToBotFramework(requestMessage, "対象の JSR は見つかりませんでした");
        }
    }

    /**
     * Get the result of Ambiguous search by keyword.
     *
     * It will be called when LUIS caluculate user would like to get JSRs from
     * ambiguous search.
     *
     * @param requestMessage messages from the clients
     * @param luis Response object from LUIS.
     */
    public void execForAmbiguousSearch(MessageFromBotFrameWork requestMessage, ResponseFromLUIS luis) {
        List<Entity> entities = Arrays.asList(luis.getEntities());
        entities.stream()
                .filter(ent -> ent.getType().equals("JSR-NAME-KEYWARD"))
                .map(Entity::getEntity)
                .findAny()
                .ifPresent((String keyword) -> {
                    final Configuration clientConfig = new ClientConfig(JacksonJsonProvider.class);
                    Client client = ClientBuilder.newClient(clientConfig);
                    WebTarget target = client.target(ENTRYPOINT_OF_JSR_AMBIGUOUS_SEARCH + keyword);
                    Response response = target
                            .request(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .get();

                    if (isRequestSuccess(response)) {
                        try {
                            List<JSRResultJSONMapping> jsrList = response.readEntity(new GenericType<List<JSRResultJSONMapping>>() {});
                            sendMessageToBotFramework(requestMessage, "お探しの JSR は" + "「" + jsrList.size() + "」 ありました。");

                            jsrList.stream().forEach(jsr -> {
                                sendMessageToBotFramework(requestMessage, "JSR : " + jsr.getId() + "「" + jsr.getNameOfJsr() + "」 がみつかりました。");
                            });

                        } catch (Exception e) {
                            LOGGER.log(Level.SEVERE, null, e);
                        }
                    } else {
                        String error = response.readEntity(String.class);
                        LOGGER.log(Level.SEVERE, error);
                        sendMessageToBotFramework(requestMessage, "お探しの項目では見つかりませんでした");
                    }
                });
    }

    /**
     * Translate from English to Japanese
     *
     * It will be called when LUIS caluculate user would like to get specific
     * JSR information.
     *
     * @param requestMessage messages from the clients
     * @param luis Response object from LUIS.
     */
    public void translateEnglish(MessageFromBotFrameWork requestMessage, ResponseFromLUIS luis) {
        List<Entity> entities = Arrays.asList(luis.getEntities());
        Integer jsrNumber = getJSRNumber(entities);
        Response response = getJSRInfo(entities);

        if (isRequestSuccess(response)) {
            JSRResultJSONMapping result = response
                    .readEntity(JSRResultJSONMapping.class);
            String englishDescription = result.getDescription();

            //Crete a instance of TranslatorTextServices
            TranslatorTextServices trans = new TranslatorTextServices();
            Optional<String> accessToken = trans.getAccessTokenForTranslator();
            accessToken.ifPresent(token -> {
                String translated = trans.translateEnglish(englishDescription, token);
                sendMessageToBotFramework(requestMessage, "JSR " + jsrNumber + " の日本語訳は 「" + translated + "」 です。<BR>"
                        + "https://jcp.org/en/jsr/detail?id=" + jsrNumber);
            });
        } else {
            String error = response.readEntity(String.class);
            LOGGER.log(Level.SEVERE, error);
            sendMessageToBotFramework(requestMessage, "対象の JSR を翻訳できませんでした");
        }
    }

    private Response getJSRInfo(List<Entity> entities) {
        Integer jsrNumber = getJSRNumber(entities);
        LOGGER.log(Level.INFO, ENTRYPOINT_OF_JSR_NUMBER_SEARCH + "{0}", jsrNumber);

        Client client = ClientBuilder.newBuilder()
                .register(JacksonFeature.class)
                .build();
        WebTarget target = client.target(ENTRYPOINT_OF_JSR_NUMBER_SEARCH + jsrNumber);
        Response response = target
                .request()
                .get();
        return response;
    }

    private Integer getJSRNumber(List<Entity> entities) {
        //JSR-WITHNUM,JSR_NUMBER
        Optional<String> jsrnum = entities.stream()
                .filter(ent -> ent.getType().equals("JSR_NUMBER"))
                .map(filterEnt -> filterEnt.getEntity())
                .findFirst();
        String jsrString = jsrnum.get();
        Integer jsrNumber;

        if (jsrString.indexOf("jar") > 0) {
            jsrNumber = Integer.valueOf(jsrString.replaceAll("jsr", ""));
        } else if (jsrString.indexOf("JSR") > 0) {
            jsrNumber = Integer.valueOf(jsrString.replaceAll("JSR", ""));
        } else {
            jsrNumber = Integer.valueOf(jsrString);
        }
        return jsrNumber;
    }

    private void sendMessageToBotFramework(MessageFromBotFrameWork requestMessage, String message) {
        String token = AccessTokenForBotService.getAccesToken();
        BotService botService = resourceContext.getResource(BotService.class);
        botService.sendResponse(requestMessage, token, message);
    }

    private void execActionForNothing(MessageFromBotFrameWork requestMessage) {
        StringBuilder builder = new StringBuilder();
        builder.append("もう一度ご入力ください。<BR/>");
        builder.append("例：<BR/>");
        builder.append("Servlet は何に含まれますか？入りますか？<BR/>");
        builder.append("JSR 299 は何ですか？<BR/>");
        builder.append("JSR 199 を翻訳してください<BR/>");
        sendMessageToBotFramework(requestMessage, builder.toString());
    }

    private boolean isRequestSuccess(Response response) {
        Response.StatusType statusInfo = response.getStatusInfo();
        Response.Status.Family family = statusInfo.getFamily();
        return family != null && family == Response.Status.Family.SUCCESSFUL;
    }

}
