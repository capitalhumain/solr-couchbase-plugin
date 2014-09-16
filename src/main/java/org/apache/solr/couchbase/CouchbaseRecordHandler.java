package org.apache.solr.couchbase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.util.JsonRecordReader.Handler;
import org.apache.solr.request.SolrQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchbaseRecordHandler implements Handler{
  
  private static final Logger LOG = LoggerFactory.getLogger(CouchbaseRequestHandler.class);
  
  SolrCAPIBehaviour capiBehaviour;
  SolrInputDocument doc;
  SolrQueryRequest req;
  int seq = 1;
  
  public CouchbaseRecordHandler(SolrCAPIBehaviour capiBehaviour, SolrQueryRequest req, SolrInputDocument doc) {
    this.capiBehaviour = capiBehaviour;
    this.doc = doc;
    this.req = req;
  } 
  
  @Override
  public void handle(Map<String, Object> record, String path) {
    SolrInputDocument solrDoc = doc.deepCopy();
    if(!path.equals("/")) {
      solrDoc.setField(CommonConstants.ID_FIELD, (String)doc.getFieldValue(CommonConstants.ID_FIELD) + "-" + seq);
      seq++;
    }
    for(Map.Entry<String, Object> entry : record.entrySet()) {
      if(entry.getKey().equals("last_modified")) {
        DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:mm:ss");
        Date date = null;
        try {
          date = (Date)formatter.parse((String) entry.getValue());
        } catch (ParseException e) {
          LOG.error("Solr Couchbase plugin could not parse date", e);
        }
        solrDoc.setField(entry.getKey(), date);
      } else {
        solrDoc.addField(entry.getKey(), entry.getValue());
      }
    }
    if((boolean)doc.getFieldValue(CommonConstants.DELETED_FIELD)) {
      capiBehaviour.deleteDoc(solrDoc.getFieldValue(CommonConstants.ID_FIELD), req);
    } else {
      capiBehaviour.addDoc(solrDoc, req);
    }
  }

}