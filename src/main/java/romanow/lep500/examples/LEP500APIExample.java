package romanow.lep500.examples;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import retrofit2.Call;
import romanow.abc.core.DBRequest;
import romanow.abc.core.UniException;
import romanow.abc.core.constants.OidList;
import romanow.abc.core.constants.Values;
import romanow.abc.core.entity.subjectarea.MeasureFile;
import romanow.abc.core.mongo.*;
import romanow.abc.desktop.APICall;
import romanow.abc.desktop.console.ConsoleClient;
import romanow.abc.desktop.console.ConsoleLogin;
import romanow.lep500.AnalyseResult;
import romanow.lep500.LEP500Params;
import romanow.lep500.fft.Extreme;
import romanow.lep500.fft.ExtremeList;

import java.util.ArrayList;
import java.util.List;

public class LEP500APIExample {
    private boolean isOn=false;
    private ConsoleClient client;
    private ArrayList<MeasureFile> measureFiles = new ArrayList<>();
    private ArrayList<LEP500Params> params = new ArrayList<>();
    private ArrayList<AnalyseResult> results = new ArrayList<>();
    public String login(){
        Values.init();
        client = new ConsoleClient();
        client.setClientIP("217.71.138.9");
        client.setClientPort(4567);
        try {
            client.startClient();
            ArrayList<String> params = new ArrayList<>();
            params.add("login");
            params.add("9137258867");
            params.add("1234");
            String log = new ConsoleLogin().exec(client,params);
            System.out.println(log);
            isOn = log.length()==0;
            return log;
        } catch (UniException e) {
            return "Клиент: "+e.toString();
        }
    }
    private final static long userOid=2;    // Романов-2 Роденко-4 Петрова-3
    public void loadFiles(){
        if (!isOn)
            return;
        DBQueryList query =  new DBQueryList().
                add(new DBQueryInt(I_DBQuery.ModeNEQ,"expertResult",0)).
                add(new DBQueryLong("userId",userOid)).
                add(new DBQueryBoolean("valid",true));
        final String xmlQuery = new DBXStream().toXML(query);
        new APICall<ArrayList<DBRequest>>(null){
            @Override
            public Call<ArrayList<DBRequest>> apiFun() {
                return client.getService().getEntityListByQuery(client.getDebugToken(),"MeasureFile",xmlQuery,1);
            }
            @Override
            public void onSucess(ArrayList<DBRequest> oo) {
                measureFiles.clear();
                for(DBRequest dd : oo){
                    try {
                        MeasureFile ss = (MeasureFile) dd.get(new Gson());
                        measureFiles.add(ss);
                    } catch (UniException e) {
                        System.out.println(e);
                    }
                }
            }
        };
    }
    public void loadParamsList(){
        params.clear();
        new APICall<ArrayList<DBRequest>>(null){
            @Override
            public Call<ArrayList<DBRequest>> apiFun() {
                return client.getService().getEntityList(client.getDebugToken(),"LEP500Params", Values.GetAllModeActual,0);
            }
            @Override
            public void onSucess(ArrayList<DBRequest> oo) {
                params.clear();
                for(DBRequest dd : oo){
                    try {
                        LEP500Params param = (LEP500Params) dd.get(new Gson());
                        params.add(param);
                    } catch (UniException e) {
                        System.out.println(e);
                    }
                }
            }
        };
    }
    public void analyseAll(int paramIdx){
        OidList list = new OidList();
        for(MeasureFile ss : measureFiles){
            list.oids.add(ss.getOid());
        }
        new APICall<ArrayList<AnalyseResult>>(null){
            @Override
            public Call<ArrayList<AnalyseResult>> apiFun() {
                return client.getService2().analyse(client.getDebugToken(),params.get(paramIdx).getOid(),list);
            }
            @Override
            public void onSucess(ArrayList<AnalyseResult> oo) {
                for(AnalyseResult dd : oo){
                    results.add(dd);
                }
            }
        };
    }
    public static void main(String ss[]){
        LEP500APIExample example = new LEP500APIExample();
        example.login();
        example.loadFiles();
        System.out.println(example.measureFiles);
        example.loadParamsList();
        System.out.println(example.params);
        example.analyseAll(0);
        for(AnalyseResult result : example.results){
            System.out.println(result.toStringFull());
            for (ExtremeList ff : result.data){
                System.out.println("Тип пиков "+ff.getExtremeMode());
                for(Extreme extreme : ff.data()){
                }
            }
        }
    }
        // Все оцененные ------------------------------------------------------------------------------------------
}
