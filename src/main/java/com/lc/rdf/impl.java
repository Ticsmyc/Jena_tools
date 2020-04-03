package com.lc.rdf;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import org.apache.commons.io.FileUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.out.NodeFmtLib;
import org.apache.jena.sparql.core.DatasetGraph;
import org.apache.jena.sparql.core.DatasetGraphFactory;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.util.FileManager;

import java.io.*;
import java.nio.charset.Charset;

/**
 * 是一个二次封装的jene工具。 在jena功能的基础上，增加了跳过错误的三元组语句功能、多线程解析功能等等等等~ 性能更好，更稳定。
 *
 * 已实现的功能：
 * 1. 统计三元组个数
 * 2. 各种格式转为nt格式
 * 3. 几种格式之间互相转换（性能不太好
 * 4. 各种格式导出csv，并且自动去掉前后缀
 * 5. csv转nt
 *
 * 继承自Koral项目，原作者是 Daniel Janke &lt;danijankATuni-koblenz.de&gt;
 *
 * @author LC
 *
 */
public class impl {
    /**
     * 统计三元组个数
     *
     * @param filepath
     */
    public static void count_triples(String filepath) {
        File inputDir = new File(filepath);
        if (!inputDir.exists()) {
            throw new IllegalArgumentException("The input " + inputDir.getAbsolutePath() + " does not exist.");
        }
        try (RDFFileIterator iter = new RDFFileIterator(inputDir, false, null);) {
            long numberOfTriples = 0;
            while (iter.hasNext()) {
                iter.next();
                numberOfTriples++;
                if ((numberOfTriples % 1_000_000) == 0) {
                    System.out.println("\t" + numberOfTriples);
                }
            }
            System.out.println("Number of triples: " + numberOfTriples);
        }
    }

    /**
     * 提取三元组的SPO,写入csv
     *
     * @param filepath
     * @param csvPath
     */
    public static void iterator_triple(String filepath,String csvPath)  {
        RDFFileIterator iter = new RDFFileIterator(new File(filepath), false, null);
        CsvWriter csvWriter = new CsvWriter(csvPath,',', Charset.forName("utf-8"));
        String[] headers = {"S","P","O"};
        int num=0;
        try{
            csvWriter.writeRecord(headers);
            for (Node[] quad : iter) {
                String[] writeLine = new String[3];
                //处理s ： 按#分割，取最后一串；再去掉最后的>
                writeLine[0]=deal_uri(serializeNode(quad[0]));
                writeLine[1]=deal_uri(serializeNode(quad[1]));
                writeLine[2]=deal_o(serializeNode(quad[2]));
                csvWriter.writeRecord(writeLine);
                num++;
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            csvWriter.close();
        }
        System.out.println("共导出"+num+"条三元组");
    }

    /**
     * 转换rdf文件格式。 （性能较差，大文件慎用
     *
     * @param filepath :原文件地址
     * @param target   ：转格式后的文件保存地址
     * @param old_format new_format   转换前后的格式  可选："RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3","TTL"
     * @return
     */
    public static void format_conversion(String filepath, String target, String old_format, String new_format) {

        Model model = ModelFactory.createDefaultModel();
        InputStream in = null;
        OutputStream out = null;
        try {
            in = FileManager.get().open(filepath);
            out = new BufferedOutputStream(new FileOutputStream(target));
            model.read(in, "", old_format);
            model.write(out, new_format);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


    /**
     * 转NT格式
     * （也许支持所有格式 ？）
     * （也许性能好一点？）
     * @param inputFile [输入文件]
     * @param outputFile   [description]
     */
    public static void Nt_conversion(String inputFile, String outputFile) {

        File inputDir = new File(inputFile);
        if (!inputDir.exists()) {
            throw new IllegalArgumentException(
                    "The input " + inputDir.getAbsolutePath() + " does not exist.");
        }

        try  {
            RDFFileIterator iter = new RDFFileIterator(inputDir, false, null);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile));
            DatasetGraph graph = DatasetGraphFactory.createGeneral();
            while(iter.hasNext()) {
                Node[] statement = iter.next();
                if (statement.length == 3) {
                    //三元组
                    graph.getDefaultGraph().add(new Triple(statement[0], statement[1], statement[2]));
                } else {
                    //四元组
                    graph.add(new Quad(statement[3], statement[0], statement[1], statement[2]));
                }
                RDFDataMgr.write(out, graph, Lang.NQ);
                graph.clear();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    /**
     * 从csv读取三元组，转为nt格式
     *
     * 注：csv格式约定：  当O是文本时，使用""包围
     *
     * @param csvPath
     * @param ntPath
     * @param prefix ：uri前缀 如 file:///C:/Data/kg/d2rq-0.8.1/
     */
    public static void csvToNt(String csvPath,String ntPath,String prefix){
        CsvReader reader=null;
        OutputStream out =null;
        try{
            out = new BufferedOutputStream(new FileOutputStream(ntPath));
            reader = new CsvReader(csvPath,',', Charset.forName("utf-8"));
            reader.readHeaders();
            DatasetGraph graph = DatasetGraphFactory.createGeneral();
            while(reader.readRecord()){
                String[] statement = reader.getValues();
                if(statement[2].contains("\"")){
                    //o中有" ，表示o是文本
                    graph.getDefaultGraph().add(new Triple(NodeFactory.createURI(prefix+statement[0]),
                            NodeFactory.createURI(prefix+statement[1]), NodeFactory.createLiteral(statement[2].replace("\"",""))));
                }else{
                    graph.getDefaultGraph().add(new Triple(NodeFactory.createURI(prefix+statement[0]),
                            NodeFactory.createURI(prefix+statement[1]), NodeFactory.createURI(prefix+statement[2])));
                }
                RDFDataMgr.write(out, graph, Lang.NQ);
                graph.clear();
            }
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            reader.close();
            try{
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行sparql查询
     * @param rdfPath
     * @param sparqlPath
     * @param outputPath
     */
    public static void sparql_query(String rdfPath,String sparqlPath,String outputPath){
        StringBuilder sparql = new StringBuilder();
        Model model = ModelFactory.createMemModelMaker().createDefaultModel();
        model.read(rdfPath);
        OutputStream os=null;
        try{

            String queryString=FileUtils.readFileToString(new File(sparqlPath),"utf-8");

            Query query = QueryFactory.create(queryString);
            QueryExecution qe = QueryExecutionFactory.create(query, model);
            ResultSet results = qe.execSelect();
            os= new BufferedOutputStream(new FileOutputStream(outputPath));
            ResultSetFormatter.out(os, results, query);
            qe.close();
        }catch(IOException e){
            e.printStackTrace();
        }finally{
            try {
                os.flush();
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private static String serializeNode(Node node) {
        return NodeFmtLib.str(node);
    }
    private static String deal_uri(String uri){
        String[] temp = uri.split("/");
        return temp[temp.length-1].substring(0,temp[temp.length-1].length()-1);
    }
    private static String deal_o(String o){
        if(o.contains("\"")){
            //是文本
            if(o.contains("//")){
                //包含了对文本类型的描述
                o=o.split("\\^")[0];
            }
            o=o.replace("\"", "");
            //前后加一个" 表示这个o是文本。
            return "\""+o+"\"";
        }
        else
            //是uri
            return deal_uri(o);
    }
}
