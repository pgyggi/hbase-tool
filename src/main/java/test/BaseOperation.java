package test;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.MasterNotRunningException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.ZooKeeperConnectionException;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
/**
 * @author Administrator
 *
 */
public class BaseOperation
{
	
	
	public static Configuration conf = null;
	
	public HTable table = null;
	
	public HBaseAdmin admin = null;
	
	static
	{
		conf = HBaseConfiguration.create();
		conf.set("hbase.zookeeper.quorum","192.168.20.48,192.168.20.49,192.168.20.51");  
        conf.set("hbase.zookeeper.property.clientPort", "2181"); 
		conf.set("zookeeper.session.timeout", "2000");
		System.out.println(conf.get("hbase.zookeeper.quorum"));
	}
	
	/**
	 * 创建一张表
	 */
	public static void creatTable(String tableName, Connection connection, String[] familys) throws Exception {
		TableName tName = TableName.valueOf(tableName);
		Admin admin = connection.getAdmin();
		if (admin.tableExists(tName)) {
			System.out.println("table already exists,now drop it first");
			deleteTable(tableName,connection);
		}
		HTableDescriptor tableDesc = new HTableDescriptor(tName);
		for (int i = 0; i < familys.length; i++) {
			tableDesc.addFamily(new HColumnDescriptor(familys[i]));
		}
		admin.createTable(tableDesc);
		System.out.println("create table " + tableName + " ok.");
	}
	
	/**
	 * 删除表
	 */
	public static void deleteTable(String tableName,Connection connection)
		throws Exception
	{
	
		try
		{
			TableName tName = TableName.valueOf(tableName);
			Admin admin = connection.getAdmin();
			admin.disableTable(tName);
			admin.deleteTable(tName);
			System.out.println("delete table " + tableName + " ok.");
		}
		catch (MasterNotRunningException e)
		{
			e.printStackTrace();
		}
		catch (ZooKeeperConnectionException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 插入一行记录
	 */
	public static void addRecord(String tableName,Connection connection, String rowKey, String family, String qualifier,
		String value)
		throws Exception
	{
	
		try
		{
			TableName tName = TableName.valueOf(tableName);
			Table table = connection.getTable(tName);
			Put put = new Put(Bytes.toBytes(rowKey));
			put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), Bytes.toBytes(value));
			table.put(put);
			System.out.println("insert recored " + rowKey + " to table " + tableName + " ok.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 插入一行记录
	 */
	public static void addImage(String tableName,Connection connection, String rowKey, String family, String qualifier,
		String imagePath)
		throws Exception
	{
	
		try
		{
			TableName tName = TableName.valueOf(tableName);
			Table table = connection.getTable(tName);
			File f1 = new File(imagePath);
			InputStream in = new BufferedInputStream(new FileInputStream(f1));
			byte buffer[] = new byte[in.available()];
			in.read(buffer);
			Put put = new Put(Bytes.toBytes(rowKey));
			put.addColumn(Bytes.toBytes(family), Bytes.toBytes(qualifier), buffer);
			table.put(put);
			in.close();
			System.out.println("insert recored " + rowKey + " to table " + tableName + " ok.");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
			
	}
	
	/**
	 * 删除一行记录
	 */
	public static void delRecord(String tableName,Connection connection, String rowKey)
		throws IOException
	{
		TableName tName = TableName.valueOf(tableName);
		Table table = connection.getTable(tName);
		List<Delete> list = new ArrayList<Delete>();
		Delete del = new Delete(rowKey.getBytes());
		list.add(del);
		table.delete(list);
		System.out.println("del recored " + rowKey + " ok.");
	}
	
	/**
	 * 查找一行记录
	 */
	public static void getOneRecord(String tableName, Connection connection,String rowKey)
		throws IOException
	{
		TableName tName = TableName.valueOf(tableName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Table table = connection.getTable(tName);
	
		Get get = new Get(rowKey.getBytes());
		Result rs = table.get(get);
		for (Cell cell : rs.listCells())
		{
			System.out.print(Bytes.toString(CellUtil.cloneRow(cell))+"=>");
			System.out.print(Bytes.toString(CellUtil.cloneFamily(cell))+":");
			System.out.print(Bytes.toString(CellUtil.cloneQualifier(cell))+"=>");
			String date = sdf.format(new Date(cell.getTimestamp()));
			System.out.println(date+"=>");
			System.out.println(Bytes.toString(CellUtil.cloneValue(cell)));
		}
	}
	/**
	 * 查找一行记录
	 */
	public static void getOneImage(String tableName, Connection connection,String rowKey,String targetPath)
		throws IOException
	{
		TableName tName = TableName.valueOf(tableName);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Table table = connection.getTable(tName);
	
		Get get = new Get(rowKey.getBytes());
		Result rs = table.get(get);
		for (Cell cell : rs.listCells())
		{
			System.out.print(Bytes.toString(CellUtil.cloneRow(cell))+"=>");
			System.out.print(Bytes.toString(CellUtil.cloneFamily(cell))+":");
			System.out.print(Bytes.toString(CellUtil.cloneQualifier(cell))+"=>");
			String date = sdf.format(new Date(cell.getTimestamp()));
			System.out.println(date+"=>");
			FileOutputStream fos = new FileOutputStream(targetPath);
			fos.write(CellUtil.cloneValue(cell));
			fos.close();
		}
	}
	
	/**
	 * 显示所有数据
	 */
	public static void getAllRecord(String tableName,Connection connection)
	{
	
		try
		{
			TableName tName = TableName.valueOf(tableName);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Table table = connection.getTable(tName);
			Scan s = new Scan();
			ResultScanner ss = table.getScanner(s);
			for (Result r : ss)
			{
				for (Cell cell : r.listCells())
				{
					System.out.print(Bytes.toString(CellUtil.cloneRow(cell))+"=>");
					System.out.print(Bytes.toString(CellUtil.cloneFamily(cell))+":");
					System.out.print(Bytes.toString(CellUtil.cloneQualifier(cell))+"=>");
					String date = sdf.format(new Date(cell.getTimestamp()));
					System.out.println(date+"=>");
					System.out.println(Bytes.toString(CellUtil.cloneValue(cell)));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] agrs)
	{
	
		try
		{
			String tablename = "scores";
			String[] familys = { "grade", "course" };
			Long start = System.currentTimeMillis();
			Connection connection = ConnectionFactory.createConnection(conf);
//			BaseOperation.creatTable(tablename,connection, familys);
			
			// add record zkb
//			BaseOperation.addRecord(tablename, connection,"zkb", "grade", "", "5");
//			BaseOperation.addImage(tablename, connection,"zkb1", "grade", "", "F:/hbase/77.jpg");
//			BaseOperation.addRecord(tablename, connection,"zkb", "course", "", "90");
//			BaseOperation.addRecord(tablename, connection,"zkb", "course", "math", "97");
//			BaseOperation.addRecord(tablename, connection,"zkb", "course", "art", "87");
			// add record baoniu
//			BaseOperation.addRecord(tablename, connection,"baoniu", "grade", "", "4");
//			BaseOperation.addRecord(tablename, connection,"baoniu", "course", "math", "89");
			
			System.out.println("===========get one record========");
//			BaseOperation.getOneRecord(tablename, connection,"zkb");
			BaseOperation.getOneImage(tablename, connection, "zkb", "./77.jpg");
			
//			System.out.println("===========show all record========");
//			BaseOperation.getAllRecord(tablename,connection);
//			
//			System.out.println("===========del one record========");
//			BaseOperation.delRecord(tablename, connection,"baoniu");
//			BaseOperation.getAllRecord(tablename,connection);
//			
//			System.out.println("===========show all record========");
//			BaseOperation.getAllRecord(tablename,connection);
			System.out.println(System.currentTimeMillis()-start);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
}
