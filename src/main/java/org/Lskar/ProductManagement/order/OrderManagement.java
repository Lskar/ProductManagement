package org.Lskar.ProductManagement.order;

import org.Lskar.ProductManagement.domain.Item;
import org.Lskar.ProductManagement.domain.Order;
import org.Lskar.ProductManagement.domain.Product;
import org.Lskar.ProductManagement.product.ProductManagement;
import org.Lskar.ProductManagement.utils.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderManagement {


    private final String orderId="order_id";

    private final String orderTime="order_time";

    private final String totalPrice="total_price";

    private final String productId="product_id";

    private final String quantity="quantity";

    private final String orderItemsAllColumn=String.format("%s,%s,%s",orderId,productId,quantity);

    private final String ordersAllColumn=String.format("%s,%s,%s",orderId,orderTime,totalPrice);

    private final ProductManagement productManagement=new ProductManagement();

    public int insertOrder(Order order){
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sql=String.format("insert into orders (%s) values(?)",totalPrice);
        try{
            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);

            if(order.getTotalPrice()<0){
                throw new IllegalPriceException("非法的订单总价！");
            }
            rs=JDBCUtil.update2(conn,ps,sql,true,order.getTotalPrice());
            if(rs.next()){
                order.setOrderID(rs.getInt(1));
            }
            insertOrderItems(conn,ps,order);
            JDBCUtil.commitTransaction(conn);
            return order.getOrderID();
        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);

        }
        return 0;
    }

    private void insertOrderItems(Connection conn,PreparedStatement ps,Order order)throws Exception{

        String sql=String.format("insert into order_items (%s) values(?,?,?)",orderItemsAllColumn);
        List<Item> items=order.getItems();
        for(Item item:items){
            Product product =productManagement.getProductById(item.getId());
            if(product==null){
                throw new ProductNotFoundException("查询产品出错！");
            }
            int result=productManagement.updateProduct(product,item.getQuantity());
            if(result==0){
                throw new ErrorInUpdateException("无法更新产品 "+product.getId()+" 的库存");
            }
            JDBCUtil.update(conn,ps,sql,order.getOrderID(),item.getId(),item.getQuantity());
        }

    }

    public int deleteOrderById(int id){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("delete from orders where %s = ?",orderId);
        try{

            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);
            int result=JDBCUtil.update(conn,ps,sql,id);
            JDBCUtil.commitTransaction(conn);
            return result;
        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return 0;
    }

    public Order getOrderById(int id){

        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        String sql=String.format("select %s from orders where %s = ?",ordersAllColumn,orderId);
        Order order=null;
        try{
            conn=JDBCUtil.getConnection();
            //JDBCUtil.startTransaction(conn);
            rs=JDBCUtil.select(conn,ps,sql,id);
            if(rs.next()){
                order=new Order(rs.getInt(orderId),rs.getTimestamp(orderTime),rs.getDouble(totalPrice));
            }
            else{
                throw new OrderNotFoundException("查询不到订单 "+id);
            }
            order.setItems(getItemsByOrderId(conn,ps,rs,id));

            return order;
        }
        catch(Exception e){
            e.printStackTrace();

        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return null;
    }

    private List<Item> getItemsByOrderId(Connection conn,PreparedStatement ps,ResultSet rs,int id) throws Exception{

        String sql=String.format("select %s from order_items where order_id = ?",orderItemsAllColumn);
        List<Item> items=new ArrayList<Item>();
        rs=JDBCUtil.select(conn,ps,sql,id);
        while(rs.next()){
            items.add(new Item(rs.getInt(productId), rs.getInt(quantity)));
        }
        return items;
    }

    public List<Order> getAllOrdersOrderBy(String orderBy){

        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        String sql=String.format("select %s from orders order by ?",ordersAllColumn);
        List<Order> orders= new ArrayList<>();
        try{
            conn=JDBCUtil.getConnection();
            //JDBCUtil.startTransaction(conn);
            rs=JDBCUtil.select(conn,ps,sql,orderBy);
//            while (rs.next()){
//                orders.add(getOrderById(rs.getInt(orderId)));
//            }
            if(!rs.next()){
                throw new OrderNotFoundException("查询不到任何订单");
            }
            else{
                do{
                    orders.add(getOrderById(rs.getInt(orderId)));
                }while(rs.next());
            }
            //JDBCUtil.commitTransaction(conn);
            return orders;
        }
        catch(Exception e){
            e.printStackTrace();
            //JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return null;
    }

    private int getOrdersCount(Connection conn,PreparedStatement ps,ResultSet rs) throws Exception{

        int count=0;
        String sql=String.format("select count(%s) count from orders",orderId);
        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,ps,sql,null);
        if(rs.next()){
            count = rs.getInt("count");
        }
        else{
            throw new OrderNotFoundException("查询不到订单记录的数量");
        }
        return count;
    }

    private List<Order> selectOrderByPage(Connection conn,PreparedStatement ps,ResultSet rs,int begin,int limit) throws Exception{
        String sql=String.format("select %s from orders limit ?,?",ordersAllColumn);
        List<Order> orders=new ArrayList<>();
        conn=JDBCUtil.getConnection();
        rs=JDBCUtil.select(conn,ps,sql,begin,limit);
        if(!rs.next()){
            throw new OrderNotFoundException("分页查询失败");
        }
        else{
            do{
                orders.add(getOrderById(rs.getInt(orderId)));
            }
            while(rs.next());
        }
        return orders;
    }

    public List<Order> getOrdersByPage(int page){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        List<Order> orders=new ArrayList<>();
        try{
            conn=JDBCUtil.getConnection();
            int limit=1;
            int totalCount=getOrdersCount(conn,ps,rs);
            int totalPage=(totalCount%limit!=0) ? totalCount / limit + 1 : totalCount / limit;
            if(page<=0){
                throw new ProductNotFoundException("非法页码！");
            }
            if(page>totalCount){
                throw new ProductNotFoundException("所查询的页码超出最大页数："+totalPage);
            }
            int begin=(page-1)*limit;
            orders=selectOrderByPage(conn,ps,rs,begin,limit);
            return orders;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return null;
    }

    private Item getItemById(Connection conn,PreparedStatement ps,ResultSet rs,int id,int itemId) throws Exception{
        String sql=String.format("select %s from order_items where %s = ? and %s = ?",orderItemsAllColumn,orderId,productId);

        rs=JDBCUtil.select(conn,ps,sql,id,itemId);
        if(rs.next()){
            return new Item(rs.getInt(productId),rs.getInt(quantity));
        }
        else{
            throw new OrderNotFoundException("找不到此条记录");
        }
    }
    private void updateItem(Connection conn,PreparedStatement ps,Item oldItem,int id,int number) throws Exception{
        String sql=String.format("update order_items set %s = ? where %s = ? and %s = ?",quantity,orderId,productId);
        JDBCUtil.update(conn,ps,sql,oldItem.getQuantity()+number,id,oldItem.getId());
    }

    private void updateOrderPriceById(Connection conn,PreparedStatement ps,int id,int itemId,int number) throws Exception{
        String sql=String.format("update orders set %s = ? where %s = ?",totalPrice,orderId);
        Order order = getOrderById(id);
        Product product = productManagement.getProductById(itemId);
        JDBCUtil.update(conn,ps,sql,order.getTotalPrice()+number*product.getPrice(),id);
    }

    private void deleteItem(Connection conn,PreparedStatement ps,int id,int itemId) throws Exception{
        String sql=String.format("delete from order_items where %s = ? and %s = ?",orderId,productId);
        JDBCUtil.update(conn,ps,sql,id,itemId);
    }




    public int updateOrderById(int id,int itemId,int number){

        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        ProductManagement productManagement=new ProductManagement();
        String sql=String.format("update orders set %s = ? where %s = ?",totalPrice,orderId);
        try{
            conn=JDBCUtil.getConnection();
            JDBCUtil.startTransaction(conn);
            Item oldItem= getItemById(conn,ps,rs,id,itemId);

            //修改order_items表
            if(oldItem.getQuantity()+number<0){
                throw new IllegalStockException("无法修改购买数量，非法的数据");
            }
            else if(oldItem.getQuantity()+number==0){
                deleteItem(conn,ps,id,itemId);
            }
            else{
                updateItem(conn,ps,oldItem,id,number);
            }

            //修改products表
            int result=productManagement.updateProduct(productManagement.getProductById(itemId),number);
            if(result==0){
                throw new IllegalStockException("无法修改库存数量");
            }

            //修改orders表
            updateOrderPriceById(conn,ps,id,itemId,number);
            
            JDBCUtil.commitTransaction(conn);
            return result;
        }
        catch(Exception e){
            e.printStackTrace();
            JDBCUtil.rollbackTransaction(conn);
        }
        finally {
            JDBCUtil.close(conn,ps,rs);
        }
        return 0;
    }






}
