package org.abithana.prescription;

import org.abithana.prescriptionBeans.PrescriptionDataBean;
import org.abithana.utill.Config;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Thilina on 1/16/2017.
 */
public class TractNeighbours implements Serializable {

    private String neighbourTableName;
    private static TractNeighbours TractNeighboursInstance;
    private Config instance=Config.getInstance();

    public static TractNeighbours getInstance(){
        if(TractNeighboursInstance==null)
            return new TractNeighbours();
        else
            return TractNeighboursInstance;
    }

    public Set<Integer> getNeighbours(int tractID){

        DataFrame df=instance.getSqlContext().sql("Select NEIGHBOR_TRACTID from "+neighbourTableName+" where SOURCE_TRACTID="+tractID);

        List<Integer> list= df.javaRDD().map(new Function<Row, Integer>() {
            public Integer call(Row row) {
                return row.getAs("NEIGHBOR_TRACTID");
            }
        }).collect();

        Set<Integer> neighbours=new HashSet<>(list);
        return neighbours;
    }

    public String getNeighbourTableName() {
        return neighbourTableName;
    }

    public void setNeighbourTableName(String neighbourTableName) {
        this.neighbourTableName = neighbourTableName;
    }
}
