package sagu.supro.BCT.dynamo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "bct-mobilehub-1215798483-UserDetails")

public class UserDetailsDO {
    private String _email;
    private String _userName;
    private String _createdDate;
    private Map<String, String> _macAddress;
    private String _password;
    private String _status;

    @DynamoDBHashKey(attributeName = "email")
    @DynamoDBAttribute(attributeName = "email")
    public String getEmail() {
        return _email;
    }

    public void setEmail(final String _email) {
        this._email = _email;
    }
    @DynamoDBRangeKey(attributeName = "userName")
    @DynamoDBAttribute(attributeName = "userName")
    public String getUserName() {
        return _userName;
    }

    public void setUserName(final String _userName) {
        this._userName = _userName;
    }
    @DynamoDBAttribute(attributeName = "createdDate")
    public String getCreatedDate() {
        return _createdDate;
    }

    public void setCreatedDate(final String _createdDate) {
        this._createdDate = _createdDate;
    }
    @DynamoDBAttribute(attributeName = "macAddress")
    public Map<String, String> getMacAddress() {
        return _macAddress;
    }

    public void setMacAddress(final Map<String, String> _macAddress) {
        this._macAddress = _macAddress;
    }
    @DynamoDBAttribute(attributeName = "password")
    public String getPassword() {
        return _password;
    }

    public void setPassword(final String _password) {
        this._password = _password;
    }
    @DynamoDBAttribute(attributeName = "status")
    public String getStatus() {
        return _status;
    }

    public void setStatus(final String _status) {
        this._status = _status;
    }

}
