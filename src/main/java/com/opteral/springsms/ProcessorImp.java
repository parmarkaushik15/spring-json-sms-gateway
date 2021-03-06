package com.opteral.springsms;

import com.opteral.springsms.database.SmsDao;
import com.opteral.springsms.exceptions.GatewayException;
import com.opteral.springsms.json.JSON_SMS;
import com.opteral.springsms.json.RequestJSON;
import com.opteral.springsms.json.ResponseJSON;
import com.opteral.springsms.json.SMS_Response;
import com.opteral.springsms.model.SMS;
import com.opteral.springsms.model.User;
import com.opteral.springsms.validation.CheckerSMS;
import com.opteral.springsms.web.WebAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProcessorImp implements Processor {

    @Autowired
    private CheckerSMS checkerSMS;

    @Autowired
    private SmsDao smsDao;

    @Autowired
    private WebAuthentication authentication;

    private boolean forDelete;
    private User user;

    public ProcessorImp()
    {

    }

    @Autowired
    public ProcessorImp(CheckerSMS checkerSMS, SmsDao smsDao, WebAuthentication authentication)
    {
        this.checkerSMS = checkerSMS;
        this.smsDao = smsDao;
        this.authentication = authentication;
    }

    @Override
    public ResponseJSON post(RequestJSON requestJSON) throws GatewayException {

        check(requestJSON);

        user = authentication.getUser();

        return new ResponseJSON(processList(requestJSON));
    }

    @Override
    public ResponseJSON delete(RequestJSON requestJSON) throws GatewayException {
        forDelete = true;
        user = authentication.getUser();
        return new ResponseJSON(processList(requestJSON));
    }

    private void check(RequestJSON requestJSON) throws GatewayException {

        checkerSMS.check(requestJSON.getSms_request());

    }

    private  List<SMS_Response> processList(RequestJSON requestJSON) throws GatewayException {

        List<SMS_Response> sms_responses = new ArrayList<SMS_Response>() ;


        for (JSON_SMS jsonSMS : requestJSON.getSms_request())
        {
            try
            {
                SMS sms = new SMS(jsonSMS, user.getId());

                persist(sms);

                sms_responses.add(new SMS_Response(sms, SMS_Response.OK));

            }
            catch (Exception e) {
                sms_responses.add(new SMS_Response(jsonSMS, SMS_Response.ERROR));
            }
        }

        return sms_responses;
    }

    private void persist(SMS sms) throws GatewayException {
        if (!sms.isTest())
        {
            if (forDelete)
                smsDao.delete(sms);
            else if (sms.getId() > 0)
                smsDao.update(sms);
            else
                smsDao.insert(sms);
        }
    }

}
