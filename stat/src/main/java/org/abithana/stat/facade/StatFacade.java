package org.abithana.stat.facade;

import org.abithana.stat.support.DataSummary;
import org.abithana.statBeans.CordinateBean;
import org.abithana.statBeans.HistogramBean;
import org.abithana.utill.Config;
import org.abithana.utill.Converter;
import org.abithana.utill.CrimeUtil;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by acer on 11/19/2016.
 */
public class StatFacade implements Serializable {

    private Config instance=Config.getInstance();

    public String queryProcessor(String query){
        return null;
    }

    public List<HistogramBean> getVisualizeList(DataFrame df){

        List<HistogramBean> list=new ArrayList<>();
        for(Row rw:df.collect()){
            HistogramBean histogramBean = new HistogramBean(""+rw.get(0),rw.getLong(1));
            list.add(histogramBean);
        }
        return list;

    }

    public List<CordinateBean> getCordinateList(DataFrame df){

        List<CordinateBean> list=new ArrayList<>();
        for(Row rw:df.collect()){
            CordinateBean cordinateBean = new CordinateBean(rw.getDouble(0),rw.getDouble(1));
            list.add(cordinateBean);
        }
        return list;
    }

    /*get data crime categorywise
    * this gives data to implement categorywise heat map
    * */
    public DataFrame categoryWiseData(DataFrame df,String[] categories){

        try {


           // crimeUtil=new CrimeUtil();
          //  df=crimeUtil.getTimeIndexedDF(df, "Dates");

            String s = " ";
            for (int i = 0; i < categories.length; i++) {
                if (i == 0)
                    s = s + " where ";

                s = s + "category=" + "'" + categories[i] + "'";
                if (i + 1 != categories.length)
                    s = s + " or ";
            }

            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select * from DataTbl " + s);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    public DataFrame categoryWiseCoordinates(DataFrame df,String[] categories){

        try {
            // crimeUtil=new CrimeUtil();
            //  df=crimeUtil.getTimeIndexedDF(df, "Dates");

            String s = " ";
            for (int i = 0; i < categories.length; i++) {
                if (i == 0)
                    s = s + " where ";

                s = s + "category=" + "'" + categories[i] + "'";
                if (i + 1 != categories.length)
                    s = s + " or ";
            }

            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select x,y from preprocessedData " + s);
            dataFrame.show(20);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    /*get data crime categorywise
       * this gives data to implement categorywise heat map
       * */
    public DataFrame categoryWiseColData(DataFrame df,String cat,String col){

        try {
            CrimeUtil crimeUtil=new CrimeUtil();
            df=crimeUtil.getTimeIndexedDF(df, "Dates");
            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select " + col + " from DataTbl where category=" + "'" + cat + "'");
            dataFrame.show(30);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    /*get data crime year wise
       * this gives data to visualize histogram
       * */
    public DataFrame categoryTimeData(DataFrame df,String cat){

        try {
            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select year,count(*) from preprocessedData where category='" + cat + "' group by year ");
            dataFrame.show(30);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    /*get data crime categorywise
   * this gives data to visualize histogram
   * */
    public DataFrame yearCategoryData(DataFrame df,int year){

        try {
            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select category,count(*) from preprocessedData where year='"+year+"' group by category ");
            dataFrame.show(30);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    public DataFrame timeWiseData(DataFrame df,int timeFrom,int timeTo){

        //convert Date to 1-24 time hours
        CrimeUtil cu=new CrimeUtil();
        df=cu.getTimeIndexedDF(df, "Dates");

        try {
            df.registerTempTable("DataTbl");
            DataFrame dataFrame = instance.getSqlContext().sql("Select * from DataTbl where time between '" + timeFrom + "' AND '" + timeTo + "'");
            dataFrame.show(50);
            return dataFrame;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }


    public List getAllFields(DataFrame dataFrame) {
        String str = dataFrame.collectAsList().toString();
        String[] FnTs = str.substring(1,str.length()-1).split(",");
        List<String> fields = new ArrayList();
        for(String s : FnTs){
            fields.add(s.split(":")[1].trim().toString());
        }
        return fields;
    }

    public List getSubFields(DataFrame dataFrame, String baseField) {
        String str = dataFrame.collectAsList().toString();
        String[] FnTs = str.substring(1,str.length()-1).split(",");
        List<String> fields = new ArrayList();
        for(String s : FnTs){
            fields.add(s.split(":")[1].trim().toString());
        }
        fields.remove((Object)baseField);
        return fields;
    }

    public DataSummary getSummary(DataFrame dataFrame,String baseField,String baseClass){
        DataSummary dataSummary = new DataSummary(baseField);
        dataSummary.setRecords(summarize(dataFrame,baseField,baseClass));
        return dataSummary;
    }

    private List<ArrayList> summarize(DataFrame dataFrame,String baseField,String baseClass){;


        List<String> subFields = new ArrayList<String>();

        dataFrame.registerTempTable("dataset");
        dataFrame.show(30);
        StringBuilder stringBuilder = new StringBuilder();
        for(String field : subFields){
            stringBuilder.append(field+",");
        }

        if(stringBuilder.length()>0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }

        String sqlQuery = "SELECT " + stringBuilder.toString() + " FROM dataset " +
                " WHERE " + baseField + " = " + "'"+baseClass+"'";

        List<ArrayList> list;
        DataFrame df = instance.getSqlContext().sql(sqlQuery);
        Converter converter = new Converter();
        list = converter.convert(df);
        return list;
    }
}
