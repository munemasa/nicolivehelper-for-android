package jp.miku39.android.nicolivehelper2;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

public class Comment {

	public Integer comment_no;
	public String text;
	public Integer premium;
	public String user_id;
	public String mail;
	public String name;
	public Long date;
	public Integer anonimity;
	
	public Comment(){
		comment_no = 0;
		text = "";
		premium = 0;
		user_id = "";
		mail = "";
		name = "";
		date = 0L;
		anonimity = 0;
	}

	public Comment(String xmlstr){
		this();

		DocumentBuilder docBuilder;

		try {
			docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = docBuilder.parse( new ByteArrayInputStream(xmlstr.getBytes("UTF-8")) );
            XPath xpath = XPathFactory.newInstance().newXPath();

            try {
				comment_no = Integer.parseInt( xpath.evaluate("/chat/@no", doc) );
			} catch (Exception e) {
				comment_no = 0;
			}
            try {
				premium = Integer.parseInt( xpath.evaluate("/chat/@premium", doc) );
			} catch (NumberFormatException e) {
				premium = 0;
			}
			text = xpath.evaluate("/chat/text()",doc);
            user_id = xpath.evaluate("/chat/@user_id",doc);
            try {
				date = Long.parseLong( xpath.evaluate("/chat/@date", doc) );
			} catch (Exception e) {
				date = 0L;
			}
            try {
				anonimity = Integer.parseInt( xpath.evaluate("/chat/@anonimity", doc) );
			} catch (NumberFormatException e) {
				anonimity = 0;
			}
            mail = xpath.evaluate("/chat/@mail", doc);
            name = xpath.evaluate("/chat/@name", doc);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
}
