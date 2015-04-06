package com.opteral.springsms.database;

import com.opteral.springsms.exceptions.GatewayException;
import com.opteral.springsms.model.SMS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component("smsdaojdbc")
public class SMSDAOJDBC implements SMSDAO{

    private static final String SELECT_SMS_BY_ID ="SELECT * FROM sms WHERE id = ?";
    private static final String UPDATE_SMS ="UPDATE sms SET sender  = ?, msisdn = ?, text = ?, datetime_scheduled = ?, subid = ?, ackurl = ?, idSMSC = ?, datetime_lastmodified = ? WHERE id = ? AND user_id = ?";
    private static final String DELETE_SMS ="DELETE FROM sms WHERE id=? AND user_id=?";

    @Autowired
    @Qualifier("jdbctemplate")
    private JdbcTemplate jdbcTemplate;

    public SMSDAOJDBC() {
    }

    public SMSDAOJDBC(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(SMS sms) throws GatewayException {

        try {
            Timestamp timestampNow = new Timestamp(new Date().getTime());

            SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate).withTableName("sms");
            jdbcInsert.setGeneratedKeyName("id");
            Map<String, Object> args = argsFromSMS(sms, timestampNow);
            long id = jdbcInsert.executeAndReturnKey(args).longValue();
            sms.setId(id);
        } catch (Exception e) {
            throw new GatewayException ("Error: Failed saving SMS");
        }
    }

    @Override
    public void update(SMS sms) throws GatewayException {
        try {
            Object[] args = new Object[] {
                    sms.getSender(),
                    sms.getMsisdn(),
                    sms.getText(),
                    sms.getDatetimeScheduled(),
                    sms.getSubid(),
                    sms.getAckurl(),
                    sms.getIdSMSC(),
                    new Timestamp(new Date().getTime()),
                    sms.getId(),
                    sms.getUser_id()
            };
            jdbcTemplate.update(UPDATE_SMS, args);

        } catch (Exception e) {
            throw new GatewayException ("Error: Failed updating sms");
        }

    }

    @Override
    public void delete(SMS sms) throws GatewayException {
        try {
            Object[] args = new Object[] {
                    sms.getId(),
                    sms.getUser_id()
            };
            jdbcTemplate.update(DELETE_SMS, args);
        } catch (EmptyResultDataAccessException e) {
            throw new GatewayException ("Error: Failed deleting sms");
        }

    }

    @Override
    public SMS getSMS(long id) throws GatewayException {
        try {
            return jdbcTemplate.queryForObject(SELECT_SMS_BY_ID, new RowMappers.SMSRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            throw new GatewayException ("Error: Failed recovering sms");
        }
    }


    private Map<String, Object> argsFromSMS(SMS sms, Timestamp timestamp)
    {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("user_id", sms.getUser_id());
        args.put("subid", sms.getSubid());
        args.put("msisdn", sms.getMsisdn());
        args.put("sender", sms.getSender());
        args.put("text", sms.getText());
        args.put("status", sms.getSms_status().getValue());
        args.put("ackurl", sms.getAckurl());
        args.put("datetime_inbound", timestamp);
        args.put("datetime_lastmodified", timestamp);
        if (sms.getDatetimeScheduled() != null)
            args.put("datetime_scheduled", new Timestamp(sms.getDatetimeScheduled().getTime()));
        else
            args.put("datetime_scheduled", null);

        return args;
    }


}