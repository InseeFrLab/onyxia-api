package fr.insee.onyxia.api.services.cassandra;

import com.datastax.driver.core.Session;

/**
 * Repository to handle the Cassandra schema.
 *
 */
public class KeyspaceRepository {
    private Session session;

    public KeyspaceRepository(Session session) {
        this.session = session;
    }

    public void useKeyspace(String keyspace) {
        session.execute("USE " + keyspace);
    }
}