package fr.insee.onyxia.api.services.cassandra;


import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Cluster.Builder;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.policies.RoundRobinPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 
 * This is an implementation of a simple Java client.
 *
 */
@Repository
@ConditionalOnProperty(name = "userdata.source", havingValue = "cassandra")
public class CassandraConnector {

    private Cluster cluster;
    private Session session;
    
	
	@Value("${cassandra.hosts}")
	private List<String> CASSANDRA_HOSTS;
	
	@Value("${cassandra.port:9042}")
	private Integer CASSANDRA_PORT;

    @PostConstruct
    public void connect() {
       List<InetAddress> addrs = CASSANDRA_HOSTS.stream().map(s -> {
         try {
            return InetAddress.getByName(s);
         } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
         }
      }).collect(Collectors.toList());
        Builder b = Cluster.builder().addContactPoints(addrs);

        b.withPort(CASSANDRA_PORT);
        b.withLoadBalancingPolicy(new RoundRobinPolicy());
        b.withoutJMXReporting();
        
        cluster = b.build();

        session = cluster.connect();
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();
    }
}