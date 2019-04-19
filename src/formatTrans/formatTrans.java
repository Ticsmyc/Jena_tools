package formatTrans;

import java.io.InputStream;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;


public class formatTrans
{
	public static void main(String args[])
	{
		//此处的文件是读取的文件，是全路径。可以是RDF的不同存储格式。
        //String inputFileName = "F:\\database\\pdd_nt\\a.nt";
		//String inputFileName = "F:\\database\\pdd_nt\\age_gender.nt";
		String inputFileName = "F:\\database\\LOL\\kglol.rdf";
		
		
		Model model = ModelFactory.createDefaultModel();
 
		InputStream in =FileManager.get().open(inputFileName);
		if (in == null) 
		{
			throw new IllegalArgumentException("File: " + inputFileName + " not found");
		}
 
		
		//根据读取文件的格式，设置read函数的参数，预定义的值有："RDF/XML","RDF/XML-ABBREV","N-TRIPLE","N3"。默认值参数是""(空值)，对应的格式为：RDF/XML.
        model.read(in, "","RDF/XML");
		//model.read(in, "","N3");
		//model.read(in, "","TTL");
 
		// 使用迭代器遍历
		StmtIterator iter = model.listStatements();
		int i=0; //控制输出数量
		while (iter.hasNext()) 
		{
			Statement stmt = iter.nextStatement(); // get next statement
			
			
			//Resource subject = stmt.getSubject(); // get the subject
			//Property predicate = stmt.getPredicate(); // get the predicate
			//RDFNode object = stmt.getObject(); // get the object
 
			String subject = stmt.getSubject().toString(); // get the subject
			String predicate = stmt.getPredicate().toString(); // get the predicate
			RDFNode object = stmt.getObject(); // get the object
			
			String[] sub = subject.split("#");
			//String sub_one=sub[sub.length-1];
			//sub=sub_one.split("/");
			System.out.print("【主语】 " + sub[sub.length-1]);	
			String[] pre = predicate.split("#");
			System.out.print("  【谓语】 " + pre[pre.length-1]);	
			
			//宾语可能是文本，也可能是实体
			if (object instanceof Resource) 
			{
				String[] obj=object.toString().split("#");
				System.out.print("  【宾语】 " + obj[obj.length-1]);
			}
			else {// object is a literal
				String[] obj=object.toString().split("E");
				System.out.print("  【宾语】 \"" + obj[0] + "\"");
			}
			System.out.println(""); 
			
			if(i++ ==500)
				//读取十条看看....
				return;
		}
	}
}