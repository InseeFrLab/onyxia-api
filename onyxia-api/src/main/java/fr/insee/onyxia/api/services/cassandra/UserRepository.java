package fr.insee.onyxia.api.services.cassandra;

import java.util.ArrayList;
import java.util.List;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import fr.insee.onyxia.model.User;

public class UserRepository {

    private static final String TABLE_NAME = "users";
    private static final String WHERE = " WHERE idep = '";

    private Session session;

    public UserRepository(Session session) {
        this.session = session;
    }

    /**
     * Insert a row in the table users.
     * @param user
     */
    public void insertUser(User user) {
        StringBuilder sb = new StringBuilder("INSERT INTO ")
        		.append(TABLE_NAME)
        		.append("(idep, nomComplet, email, sshPublicKey, password) ")
        		.append("VALUES ('")
        		.append(user.getIdep())
        		.append("', '")
        		.append(user.getNomComplet())
        		.append("', '")
        		.append(user.getEmail())
        		.append("', '")
        		.append(user.getSshPublicKey())
        		.append("', '")
        		.append(user.getPassword())
        		.append("');");

        final String query = sb.toString();
        session.execute(query);
    }
    
    public void updateUser(User user ) {
    	StringBuilder sb = new StringBuilder("UPDATE ")
    			.append(TABLE_NAME)
    			.append(" SET sshPublicKey = '")
    			.append(user.getSshPublicKey())
    			.append("', password = '")
    			.append(user.getPassword())
    			.append("' ")
    			.append(WHERE)
    			.append(user.getIdep())
    			.append("';");
    	
    	final String query = sb.toString();
        session.execute(query);
    }
    
    public void upgradeUser(User user) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME)
        		.append(WHERE).append(user.getIdep()).append("';");

        final String query = sb.toString();

        ResultSet rs = session.execute(query);

        

        for (Row r : rs) {
            
            user.setSshPublicKey(r.getString("sshPublicKey"));
            user.setPassword(r.getString("password"));
        }
        
//        Arrays.<User>asList().addAll(rs.iterator().s)

    }

    /**
     * Select user by idep.
     * 
     * @return
     */
    public User selectByIdep(String idep) {
        StringBuilder sb = new StringBuilder("SELECT * FROM ").append(TABLE_NAME)
        		.append(WHERE).append(idep).append("';");

        final String query = sb.toString();

        ResultSet rs = session.execute(query);

        List<User> users = new ArrayList<User>();

        for (Row r : rs) {
            users.add(
            		User.newInstance()
            			.setEmail(r.getString("email"))
            			.setIdep(r.getString("idep"))
            			.setNomComplet(r.getString("nomComplet"))
            			.setSshPublicKey(r.getString("sshPublicKey"))
            			.setPassword(r.getString("password"))
            			.build()
            		);
        }
        
//        Arrays.<User>asList().addAll(rs.iterator().s)

        if (! users.isEmpty()) {
        	return users.get(0);
        } else {
        	return null;
        }
    }


    /**
     * Delete a user by idep.
     */
    public void deleteUser(String idep) {
        StringBuilder sb = new StringBuilder("DELETE FROM ").append(TABLE_NAME)
        		.append(WHERE).append(idep).append("';");

        final String query = sb.toString();
        session.execute(query);
    }
}