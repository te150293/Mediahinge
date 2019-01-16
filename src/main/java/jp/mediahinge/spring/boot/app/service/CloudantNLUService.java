package jp.mediahinge.spring.boot.app.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import jp.mediahinge.spring.boot.app.form.NLUForm;

@Service
public class CloudantNLUService extends CloudantService{

    public Collection<NLUForm> getAll(){
        List<NLUForm> docs;
        try {
            docs = getDB().getAllDocsRequestBuilder().includeDocs(true).build().getResponse().getDocsAs(NLUForm.class);
        } catch (IOException e) {
            return null;
        }
        return docs;
    }

    public NLUForm get(String id) {
        return getDB().find(NLUForm.class, id);
    }

    public NLUForm persist(NLUForm NLUForm) {
        String id = getDB().save(NLUForm).getId();
        return getDB().find(NLUForm.class, id);
    }

    public void delete(String id) {
        NLUForm NLUForm = getDB().find(NLUForm.class, id);
        getDB().remove(id, NLUForm.get_rev());

    }

    public int count() throws Exception {
        return getAll().size();
    }

}
