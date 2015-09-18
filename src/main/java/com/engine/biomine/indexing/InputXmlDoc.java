

package com.engine.biomine.indexing;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName = "art") //root element name

public class InputXmlDoc {
	
	String id;
    String ui;
    String ji;
    long fm;
    String bm;

    public InputXmlDoc() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    

    public String getUi() {
        return ui;
    }

    public void setUi(String ui) {
        this.ui = ui;
    }

    public long getFm() {
        return fm;
    }

    public void setFm(long fm) {
        this.fm = fm;
    }

    public String getJi() {
        return ji;
    }

    public void setJi(String ji) {
        this.ji = ji;
    }

    public String getBm() {
        return bm;
    }

    public void setBm(String bm) {
        this.bm = bm;
    }

}
