package fi.vrk.xroad.fileservice.service;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;


@Configuration
@EnableAutoConfiguration
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EndpointImpl fileServiceEndpoint(Bus bus) {
        EndpointImpl endpoint = new EndpointImpl(bus, fileService());
        endpoint.publish("/fileservice");
        return endpoint;
    }

    @Bean
    public XroadFileService fileService() {
        return new XroadFileServiceImpl();
    }

    @Bean("identifiers")
    public JAXBContext identifiersContext() throws JAXBException {
        return JAXBContext.newInstance(eu.x_road.xsd.identifiers.ObjectFactory.class);
    }

}
