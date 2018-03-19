# xroad-fileservice

File Service - a sample web service for transferring files over X-Road. Currently supports only serving files.
The file contents are returned using [MTOM](https://www.w3.org/Submission/soap11mtom10)

## Option 1: install jar as a package from reposotory - automatic execution as a service
### Installation
Debian and Redhat release packages are available in repository: 
http://www.nic.funet.fi/pub/csc/x-road/xroad-fileservice/
  
Supported versions: Ubuntu 14.04 and RHEL7

#### Ubuntu
Precondition: Ubuntu 14 or earlier requires adding repository for java8-runtime-headless before fileservice installation:  
```sudo add-apt-repository ppa:openjdk-r/ppa```  
```apt-get update```  

Install service (Ubuntu 14.xx/upstart will start the service automatically):  
```sudo apt install xroad-fileservice```


Verify service is running:  
```initctl status xroad-fileservice```

Using service:  
```initctl stop xroad-fileservice```  
```initctl start xroad-fileservice```

#### Redhat
Install service:  
```sudo yum install xroad-fileservice```

Redhat/system.d requires manual service start:  
```systemctl start xroad-fileservice```
    
Verify service is running:  
```systemctl status xroad-fileservice```
    
Using service:  
```systemctl stop xroad-fileservice```  
```systemctl start xroad-fileservice```

## Option 2: build jar from sources and execute manually
### Building

Requires JDK 8 to build and JRE 8 to run.

    git clone https://github.com/vrk-kpa/xroad-fileservice
    cd xroad-fileservice
    ./gradlew build

The build produces two runnable jars: the  service (in service/build/libs) and a simple client (in client/build/libs)

### Running the Service

1. Create a directory for downloadable files (default location /var/spool/xroad-fileservice/outgoing)

    ```[sudo] mkdir -p /var/spool/xroad-fileservice/outgoing```  
    The current version does not support subdirectories.
    
2. Create a directory for incoming files (default location /var/spool/xroad-fileservice/incoming)

    ```[sudo] mkdir -p /var/spool/xroad-fileservice/incoming```  
    Change permissions so that writing is allowed by the server process. 
    Overwriting an existing file is not allowed.
 
2. Run the service:
    ```
    java -jar service/build/libs/xroad-fileservice.jar \
        --server.port=8080 \
        --outgoing-directory=/var/spool/xroad-fileservice/outgoing
        --incoming-directory=/var/spool/xroad-fileservice/incoming
    ```
    The parameters are optional if the default values (above) are used.

The service [WSDL](src/main/resources/fileservice.wsdl) is available from http://\<host:port\>/fileservice?wsdl

## Using the client
Without parameters, a short usage note is outputted:

    java -jar client/build/libs/xroad-fileclient-1.0.jar
    Usage: (java -jar ...) <url> <clientId> <memberId> <command> [command arguments]
    	url     : service or client security server URL
    	clientId: instanceId/memberClass/memberCode/subsystemCode
    	memberId: service memberId, same format as clientId
    	filename: name of the file to fetch
    	command : get | put | list
    
    	          get <remote filename> [local filename]
    	          remote filename : name of the remote file to fetch
    	          local filename  : name of the output file, or standard output if omitted
    
    	          put <local filename> [remote file name]
    	          local filename  : name of the input file, or '-' for standard input
    	          remote filename : name of the remote file (same as local file if omitted)
    
    	          list
    	          (lists downloadable files)


**Download a file**

    java -jar client/build/libs/xroad-fileclient.jar \
    http://localhost:8080/fileservice \
    INSTANCE/CLASS/MEMBER/CLIENTSUBSYSTEM \
    INSTANCE/CLASS/MEMBER/SERVICESUBSYSTEM \
    get \
    remotefile \
    localfile

**Upload a file**    
    
    java -jar client/build/libs/xroad-fileclient.jar \
    http://localhost:8080/fileservice \
    INSTANCE/CLASS/MEMBER/CLIENTSUBSYSTEM \
    INSTANCE/CLASS/MEMBER/SERVICESUBSYSTEM \
    put \
    localfile \
    remotefile
    
**List downloadable files**

    java -jar client/build/libs/xroad-fileclient.jar \
    http://localhost:8080/fileservice \
    INSTANCE/CLASS/MEMBER/CLIENTSUBSYSTEM \
    INSTANCE/CLASS/MEMBER/SERVICESUBSYSTEM \
    list

## Testing without the client

Example request (using curl)  
note: In Ubuntu default port is 8070
```
curl -H 'Content-Type:text/xml' --data-binary @-  http://localhost:8080/fileservice <<EOF
<soapenv:Envelope
    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
    xmlns:xro="http://x-road.eu/xsd/xroad.xsd"
    xmlns:iden="http://x-road.eu/xsd/identifiers"
    xmlns:f="http://vrk.fi/xroad/fileservice">
   <soapenv:Header>
      <xro:protocolVersion>4.0</xro:protocolVersion>
      <xro:id>ID</xro:id>
      <xro:userId>userID</xro:userId>
      <xro:service iden:objectType="SERVICE">
         <iden:xRoadInstance>FI</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>TEST</iden:memberCode>
         <iden:subsystemCode>FILESERVICE</iden:subsystemCode>
         <iden:serviceCode>get</iden:serviceCode>
      </xro:service>
      <xro:client iden:objectType="SUBSYSTEM">
         <iden:xRoadInstance>FI</iden:xRoadInstance>
         <iden:memberClass>GOV</iden:memberClass>
         <iden:memberCode>CLIENT</iden:memberCode>
         <iden:subsystemCode>SUB</iden:subsystemCode>
      </xro:client>
   </soapenv:Header>
   <soapenv:Body>
      <f:get>
         <f:name>filename</f:name>
      </f:get>
   </soapenv:Body>
</soapenv:Envelope>
EOF
```
Example response (file 'filename' was not found):
```
--uuid:65c5f738-f22c-4ff5-b16d-8e16ba34c791
Content-Type: application/xop+xml; charset=UTF-8; type="text/xml"
Content-Transfer-Encoding: binary
Content-ID: <root.message@cxf.apache.org>

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Body>
<soap:Fault>
<faultcode>soap:Server</faultcode>
<faultstring>The requested file does not exist</faultstring>
<detail>
<errorResponse xmlns="http://vrk.fi/xroad/fileservice"><error>File not found: filename</error></errorResponse>
</detail>
</soap:Fault>
</soap:Body>
</soap:Envelope>
--uuid:65c5f738-f22c-4ff5-b16d-8e16ba34c791--
```
Example response (returning the requested file):
```
--uuid:376f75ad-8433-4a31-b361-665d42342484
Content-Type: application/xop+xml; charset=UTF-8; type="text/xml"
Content-Transfer-Encoding: binary
Content-ID: <root.message@cxf.apache.org>

<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
<soap:Header>
 <xro:protocolVersion xmlns:xro="http://x-road.eu/xsd/xroad.xsd">4.0</xro:protocolVersion>
 <xro:id xmlns:xro="http://x-road.eu/xsd/xroad.xsd">ID</xro:id>
 <xro:userId xmlns:xro="http://x-road.eu/xsd/xroad.xsd">userID</xro:userId>
 <xro:service xmlns:xro="http://x-road.eu/xsd/xroad.xsd" xmlns:iden="http://x-road.eu/xsd/identifiers" iden:objectType="SERVICE">
  <iden:xRoadInstance>FI</iden:xRoadInstance>
  <iden:memberClass>GOV</iden:memberClass>
  <iden:memberCode>TEST</iden:memberCode>
  <iden:subsystemCode>FILESERVICE</iden:subsystemCode>
  <iden:serviceCode>get</iden:serviceCode>
 </xro:service>
 <xro:client xmlns:xro="http://x-road.eu/xsd/xroad.xsd" xmlns:iden="http://x-road.eu/xsd/identifiers" iden:objectType="SUBSYSTEM">
  <iden:xRoadInstance>FI</iden:xRoadInstance>
  <iden:memberClass>GOV</iden:memberClass>
  <iden:memberCode>CLIENT</iden:memberCode>
  <iden:subsystemCode>SUB</iden:subsystemCode>
 /xro:client>
</soap:Header>
<soap:Body>
 <getResponse xmlns="http://vrk.fi/xroad/fileservice">
  <object>
   <xop:Include xmlns:xop="http://www.w3.org/2004/08/xop/include" href="cid:6b062298-6a45-4466-bc13-ce2efcccfa70-8@vrk.fi"/>
  </object>
 </getResponse>
</soap:Body>
</soap:Envelope>
--uuid:376f75ad-8433-4a31-b361-665d42342484
Content-Type: application/octet-stream
Content-Transfer-Encoding: binary
Content-ID: <6b062298-6a45-4466-bc13-ce2efcccfa70-8@vrk.fi>
Content-Disposition: attachment;name="foo"

bar
```
