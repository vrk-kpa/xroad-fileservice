/**
 * MIT License
 *
 * Copyright (c) 2017 Population Register Centre
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package fi.vrk.xroad.fileservice.client;

import eu.x_road.xsd.identifiers.XRoadObjectType;
import eu.x_road.xsd.identifiers.XRoadServiceIdentifierType;

class XRoadServiceIdBuilder {
    private final String xRoadInstance;
    private final String memberClass;
    private final String memberCode;
    private final String subsystemCode;
    private final String serviceVersion;

    XRoadServiceIdBuilder(String xRoadInstance, String memberClass, String memberCode, String subsystemCode,
            String serviceVersion) {
        this.xRoadInstance = xRoadInstance;
        this.memberClass = memberClass;
        this.memberCode = memberCode;
        this.subsystemCode = subsystemCode;
        this.serviceVersion = serviceVersion;
    }

    XRoadServiceIdentifierType build(String serviceCode) {
        final XRoadServiceIdentifierType serviceId = new XRoadServiceIdentifierType();
        serviceId.setObjectType(XRoadObjectType.SERVICE);
        serviceId.setXRoadInstance(xRoadInstance);
        serviceId.setMemberClass(memberClass);
        serviceId.setMemberCode(memberCode);
        serviceId.setSubsystemCode(subsystemCode);
        serviceId.setServiceVersion(serviceVersion);
        serviceId.setServiceCode(serviceCode);
        return serviceId;
    }
}
