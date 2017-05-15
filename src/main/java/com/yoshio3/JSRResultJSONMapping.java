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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * JSON mapping class by using JAX-B.
 * 
 * After received the response which wrapped by JSON,
 * this class will mapping to the JSON Object.
 *
 * @author Yoshio Terada
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties( ignoreUnknown = true )
public class JSRResultJSONMapping implements Serializable{

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer jsrId;
    private String description;
    private String nameOfJsr;
    private String reason;
    private String[] specLeads;
    private Date startDate;
    private Date endDate;
    private Date effectiveDate;
    private String latestStage;
    private String currentStatus;

    public JSRResultJSONMapping() {
    }

    public JSRResultJSONMapping(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getJsrId() {
        return jsrId;
    }

    public void setJsrId(Integer jsrId) {
        this.jsrId = jsrId;
    }

    public String getLatestStage() {
        return latestStage;
    }

    public void setLatestStage(String latestStage) {
        this.latestStage = latestStage;
    }

    public String getNameOfJsr() {
        return nameOfJsr;
    }

    public void setNameOfJsr(String nameOfJsr) {
        this.nameOfJsr = nameOfJsr;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String[] getSpecLeads() {
        return specLeads;
    }

    public void setSpecLeads(String[] specLeads) {
        this.specLeads = specLeads;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(String currentStatus) {
        this.currentStatus = currentStatus;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final JSRResultJSONMapping other = (JSRResultJSONMapping) obj;
        return Objects.equals(this.id, other.id);
    }

    @Override
    public String toString() {
        return "JsrList{" + "id=" + id + ", description=" + description + ", effectiveDate=" + effectiveDate + ", endDate=" + endDate + ", jsrId=" + jsrId + ", latestStage=" + latestStage + ", nameOfJsr=" + nameOfJsr + ", reason=" + reason + ", specLeads=" + Arrays.toString(specLeads) + ", startDate=" + startDate + ", currentStatus=" + currentStatus + '}';
    }
}

