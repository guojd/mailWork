package com.pt.myworkcenter.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DosService {
    public List<Map<String,Object>> getNetsh(Map<String,Object> params){
        List<String> results=exec("netsh interface portproxy show all");
        List<Map<String,Object>> responseList=new ArrayList<>();
        boolean flag=false;
        String regex = "\\S+";
        String[] fieldName=new String[]{"listenaddress","listenport","connectaddress","connectport"};
        Pattern pattern = Pattern.compile(regex);
        for (String result:results) {
            if(result.contains("-------")){
                flag=true;
            }
            else if(result.length()==0){
                flag=false;
            }
            else if(flag){
                Matcher matcher = pattern.matcher(result);
                Map<String,Object> item=new HashMap<>();
                int i=0;
                while (matcher.find()) {
                    String group = matcher.group();
                    item.put(fieldName[i],group);
                    i++;
                }
                responseList.add(item);
            }
        }
        return responseList;
    }
    public String add(Map<String,Object> params){
        List<String> result=exec("netsh interface portproxy add v4tov4" +
                " listenaddress="+params.get("listenaddress")+" listenport="+params.get("listenport")+
                " connectaddress="+params.get("connectaddress")+" connectport="+params.get("connectport"));
        return result.size()>0&&result.get(0).length()>0?result.get(0):"添加成功";
    }
    public String del(Map<String,Object> params){
        List<String> result=exec("netsh interface portproxy delete v4tov4" +
                " listenaddress="+params.get("listenaddress")+" listenport="+params.get("listenport"));
        return result.size()>0&&result.get(0).length()>0?result.get(0):"删除成功";
    }
    private List<String> exec(String dos){
        List<String> results=new ArrayList<>();
        Runtime rt = Runtime.getRuntime();
        try {
            Process pr = rt.exec("cmd /c "+dos);
            BufferedReader input = new BufferedReader(new InputStreamReader(pr.getInputStream(), "GBK"));
            String line = null;
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                results.add(line);
            }
            int exitVal = pr.waitFor();
            System.out.println("Exited with error code " + exitVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    static public void main(String[] arg){
        DosService dosService=new DosService();
        dosService.getNetsh(null);
    }
}
