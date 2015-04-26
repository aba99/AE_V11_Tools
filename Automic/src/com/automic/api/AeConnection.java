package com.automic.api;

import com.uc4.api.Template;
import com.uc4.api.UC4ObjectName;
import com.uc4.api.UC4UserName;
import com.uc4.api.objects.*;
import com.uc4.api.systemoverview.ClientListItem;
import com.uc4.communication.Connection;
import com.uc4.communication.requests.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Connection to the Automation Engine. Performs provisioning specific tasks.
 */
public class AeConnection implements Closeable {

    private final static Logger LOGGER = LoggerFactory.getLogger(AeConnection.class);

    private final Connection connection;

    public AeConnection(Connection connection) {
        this.connection = connection;
    }

    public int findNextFreeClient(int offset) {
        LOGGER.info("Searching for next free client starting from " + offset);

        ClientList clientList = new ClientList();
        send(clientList);
        LOGGER.info("Found " + clientList.size() + " clients");
        Map<Integer, ClientListItem> clientListItemHashMap = new HashMap<>();
        for (ClientListItem clientListItem : clientList) {
            clientListItemHashMap.put(clientListItem.getClient(), clientListItem);
        }
        int newClient = offset;
        while (clientListItemHashMap.containsKey(newClient)) {
            newClient++;
        }
        if (newClient > 9999) {
            LOGGER.error("Could not find free client id");
        }
        LOGGER.info("Next free client: " + newClient);
        return newClient;
    }

    public void createClient(int client, String title) {
        LOGGER.info("Creating new client: " + client);

        IFolder folder = getNoFolder();
        TemplateList templateList = new TemplateList();
        send(templateList);

        Template template = templateList.getTemplate("CLNT");
        if (template == null) {
            LOGGER.error("Template 'CLNT' not found");
            throw new AeException("Template 'CLNT' not found");
        }

        String newClientName = new DecimalFormat("0000").format(client);
        String tempClientName = "CLNT" + newClientName;
        CreateObject createObject = new CreateObject(new UC4ObjectName(tempClientName),
                template, folder);
        send(createObject);
        LOGGER.info("Created object " + tempClientName);

        // now rename the client and remove the prefix:
        RenameObject renameObject = new RenameObject(new UC4ObjectName(tempClientName),
                new UC4ObjectName(newClientName), folder, title);
        send(renameObject);

        LOGGER.info("Renamed object " + tempClientName + " to " + newClientName);
        LOGGER.info("Client '" + newClientName + "' created");
    }

    /**
     * Create an admin user with temporary authorizations to create the 'Analytics as a Service' groups in new client
     */
    public void createAdminForClient(int targetClient, UC4UserName username, String firstname, String lastname, String email, String userPassword) {
        LOGGER.info("Create admin user " + username + " and move to client: " + targetClient);

        //create user with tmp name
        IFolder folder = getNoFolder();
        CreateObject createObject = new CreateObject(username,
                Template.USER, folder);
        send(createObject);
        LOGGER.info("Created user:" + username);

        setupPropertiesAndPrivileges(username, firstname, lastname, email, userPassword);

        LOGGER.info("Temporary privileges and authorizations saved, move user to client now");

        // move user to target client
        MoveUserToClient moveUserToClient = new MoveUserToClient(username, folder, targetClient);
        send(moveUserToClient);
        LOGGER.info("User '" + username + "' moved to client '" + targetClient + "' successfully");
    }



    private void saveObject(UC4Object user) {
        SaveObject saveObject;
        try {
            saveObject = new SaveObject(user);
        } catch (InvalidObjectException e) {
            throw new AeException(String.format("Unable to save object '%s'", user), e);
        }
        send(saveObject);
    }

    public void renameUser(UC4UserName oldName, UC4UserName newName) {
        FolderTree tree = new FolderTree();
        send(tree);
        IFolder folder = tree.getFolderByID("0");
        LOGGER.info("Rename user from '{}' to '{}'", oldName, newName);
        RenameObject rename = new RenameObject(oldName, newName, folder, "Initial User");
        send(rename);
    }

    public UC4Object createAndOpenObject(String name, Template template) {
        IFolder folder = getNoFolder();
        return createAndOpenObject(name, folder, template);
    }

    public UC4Object createAndOpenObject(String name, IFolder folder, Template template) {
        CreateObject createObject = new CreateObject(new UC4ObjectName(name),
                template, folder);
        send(createObject);
        OpenObject openObject = new OpenObject(new UC4ObjectName(name));
        send(openObject);
        LOGGER.info(openObject.getUC4Object().getName()+" created in "+folder.fullPath());

        return openObject.getUC4Object();

    }
    
    public UC4Object duplicateAndOpenObject(String source_name,String dest_name, IFolder folder) {
        
    	dest_name=AeStringUtils.makeAeValid(dest_name);
    	DuplicateObject duplicateObject = 
        		new DuplicateObject(new UC4ObjectName(source_name),new UC4ObjectName(dest_name),folder);
        send(duplicateObject);
        OpenObject openObject = new OpenObject(new UC4ObjectName(dest_name));
        send(openObject);
        LOGGER.info(dest_name+" created in "+folder.fullPath());
        return openObject.getUC4Object();
    }


    public void saveAndCloseObject(UC4Object obj) {
        saveObject(obj);
        CloseObject closeObject = new CloseObject(obj);
        send(closeObject);
    }

    public void close() throws IOException {
        connection.close();
    }

    public IFolder createFolder(String folderPath) {
        String[] subFolders = folderPath.split("/");
        for (int i = 0; i < subFolders.length; i++) {

            StringBuilder currentPath = new StringBuilder(subFolders[0]);
            for (int j = 1; j <= i; j++) {
                currentPath.append("/");
                currentPath.append(subFolders[j]);
            }

            FolderTree tree = new FolderTree();
            send(tree);

            if (tree.getFolder(currentPath.toString()) == null) {
                IFolder root = tree.root();
                if (i > 0) {
                    StringBuilder parentPath = new StringBuilder(subFolders[0]);
                    for (int j = 1; j < i; j++) {
                        parentPath.append("/");
                        parentPath.append(subFolders[j]);
                    }
                    root = tree.getFolder(parentPath.toString());
                }
                CreateObject create = new CreateObject(new UC4ObjectName(
                        subFolders[i]), Template.FOLD, root);
                send(create);
            }

        }
        return getFolderByPath(folderPath);
    }

    private IFolder getNoFolder() {
        FolderTree tree = new FolderTree();
        send(tree);
        return tree.getFolderByID("0");
    }

    @SuppressWarnings("unused")
	private IFolder getRootFolder() {
        FolderTree tree = new FolderTree();
        send(tree);
        return tree.root();
    }

    private IFolder getFolderByPath(String path) {
        FolderTree tree = new FolderTree();
        send(tree);
        return tree.getFolder(path);
    }

    private void send(XMLRequest req) {
        try {
            connection.sendRequestAndWait(req);
            if (req.getMessageBox() != null) {
                throw new AeException(req.getMessageBox().getText());
            }
        } catch (IOException e) {
            throw new AeException("Unable to send XMLRequest", e);
        }
    }

    private void setupPropertiesAndPrivileges(UC4UserName username, String firstname, String lastname, String email, String userPassword) {
        //open user and set pw and authorizations
        OpenObject openObject = new OpenObject(username);
        send(openObject);

        User user = (User) openObject.getUC4Object();

        try {
            LOGGER.info("Set user privileges ");
            //required to allow rename of own userobj as it is in folder 0
            user.privileges().setPrivilege(UserPrivileges.Privilege.WORK_IN_RUNBOOK_MODE, false);
            user.privileges().selectAll();
            user.attributes().setPassword(userPassword);
            user.attributes().setFirstName(firstname);
            user.attributes().setLastName(lastname);
            user.attributes().setEmail1(email);
            user.attributes().setPasswordChangeRequired(true);

            LOGGER.info("Set user authorizations");
            UserRight userRight = new UserRight();
            userRight.setGrp(1);
            userRight.setType(UserRight.Type.ALL);
            userRight.setName("*");
            userRight.setHost("*");
            userRight.setFileNameSource("*");
            userRight.setFileNameDestination("*");
            userRight.setLoginDestination("*");
            userRight.setDefineSLA(true);
            userRight.setExecute(true);
            userRight.setDelete(true);
            userRight.setCancel(true);
            userRight.setStatistics(true);
            userRight.setRead(true);
            userRight.setWrite(true);
            userRight.setReport(true);
            userRight.setModifyAtRuntime(true);
            user.authorizations().addRight(userRight);
        } finally {
            saveAndCloseObject(user);
        }
        
        
    }
    
    public IFolder getFolder(String a)
    {
    	 FolderTree tree = new FolderTree();
         send(tree);
         IFolder folder = tree.getFolder(a);
         return folder;
    }
    
    public void openAndEditAndSaveAndCloseConnectionObject(UC4ObjectName objName,String user,String pass,String host, String port, String protocol) {

    	OpenObject openObject = new OpenObject(objName);
        send(openObject);

       UC4Object connObj = openObject.getUC4Object();
       RAConnection conn = (RAConnection)connObj;
       OCVPanel fields = conn.ocvValues();
        try {
            LOGGER.info("Setting fields for : "+connObj.getName());
 

            fields.setValue("hostName", host, false, false);
            fields.setValue("password", pass, false, false);
            fields.setValue("port", port, false, false);
            fields.setValue("userId", user, false, false);
            fields.setValue("operation", protocol, false, false);


            

        } finally {
            saveAndCloseObject(conn);
        }
        
        
    }
    
    public void editVaraObject(UC4ObjectName objName,String user,String pass,String host, String port, String protocol) {

    	OpenObject openObject = new OpenObject(objName);
        send(openObject);

       UC4Object connObj = openObject.getUC4Object();
       RAConnection conn = (RAConnection)connObj;
       OCVPanel fields = conn.ocvValues();
        try {
            LOGGER.info("Setting fields for : "+connObj.getName());
 

            fields.setValue("hostName", host, false, false);
            fields.setValue("password", pass, false, false);
            fields.setValue("port", port, false, false);
            fields.setValue("userId", user, false, false);
            fields.setValue("operation", protocol, false, false);


            

        } finally {
            saveAndCloseObject(conn);
        }
        
        
    }
    
       
    public boolean isFound(String objName) {//looks up conn obj and vara obj

    	SearchObject search = new SearchObject();
    	search.setName(objName);
    	
    	search.setTypeCONN(true);
    	search.setTypeVARA(true);
        send(search);
       
        if(search.size()!=0)
        {
        	return true;
        }
        else
        {
        	return false;
        }  
        
    }
    
    public void deleteObject(String objName)
    {
    	if(isFound(objName))
    	{
    		DeleteObject deletion = new DeleteObject(new UC4ObjectName (objName));
    		send(deletion);
    		LOGGER.info(objName+" deleted");
    	}
    }
    
}
