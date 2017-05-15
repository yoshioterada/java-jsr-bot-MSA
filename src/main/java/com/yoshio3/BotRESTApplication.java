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


import com.yoshio3.rest.entities.bot.BotObjectMapperProvider;
import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 * At first, You need to write your bot service as a Web Application. 
 * It means that you need to create some 
 * 
 * I refer to the following documents to create this services. It is very useful
 * to understand the bot REST call.
 * https://blogs.msdn.microsoft.com/tsmatsuz/2016/08/19/build-skype-bot-with-microsoft-bot-framework-oauth-and-rest-api/
 * 
 * At first, please download the ngrok application from following site?
 * https://ngrok.com
 * 
 * After download it, pleasse execute following command to tunnell the localserver to public?
 * ./ngrok http 8080
 * 
 * The above means that if you run the WebServer or Application server on localhost with 8080 port number,
 * You can access to the site by using public URL like follows.
 * Forwarding                    http://be53b1e8.ngrok.io -> localhost:8080                                                   
 * Forwarding                    https://be53b1e8.ngrok.io -> localhost:8080                                                  
 * 
 * Then please access to the following URL to manage your bot?
 * https://dev.botframework.com/bots
 * 
 * 
 * 
 * @author Yoshio Terada
 */

@ApplicationPath("/rest")
public class BotRESTApplication extends ResourceConfig {

    public BotRESTApplication() {
        packages(BotRESTApplication.class.getPackage().getName());
        super.register(BotObjectMapperProvider.class);
        super.register(JacksonFeature.class);
    }
}
