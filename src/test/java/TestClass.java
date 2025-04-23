import org.Lskar.ProductManagement.domain.Item;
import org.Lskar.ProductManagement.domain.Order;
import org.Lskar.ProductManagement.domain.Product;
import org.Lskar.ProductManagement.order.OrderManagement;
import org.Lskar.ProductManagement.product.ProductManagement;
import org.Lskar.ProductManagement.utils.JDBCUtil;
import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;


public class TestClass {
    @Test
    public void insertProductTest() {

        ProductManagement productManagement = new ProductManagement();
        List<Product> products = new ArrayList<>();
        Collections.addAll(products,
                new Product("小狗",600,999),
                new Product("小猫",500,999),
                new Product("薯片",5,888),
                new Product("可乐",3,777),
                new Product("巧克力",10,666)
        );
        for (Product product : products) {
            int result = productManagement.insertProduct(product);
            if(result == 0) {
                System.out.println("Product "+product.getName()+" inserted failed");
            }
        }
    }

    @Test
    public void selectProductByIdTest() {
        ProductManagement productManagement= new ProductManagement();
        Product product = productManagement.getProductById(4);
        if(product!=null){
            System.out.println(product);
        }
        else{
            System.out.println("Product not found!");
        }
    }

    @Test
    public void selectAllProductsTest() {
        List<Product> productList= new ArrayList<Product>();
        ProductManagement productManagement= new ProductManagement();
        productList=productManagement.getAllProducts("price");
        if (productList!=null){
            for (Product product : productList) {
                System.out.println(product);
            }
        }
        else {
            System.out.println("Product not found!");
        }
    }

    @Test
    public void updateProductStockByIdTest() {
        ProductManagement productManagement= new ProductManagement();
        //传入购买数量
        int result=productManagement.updateProduct(productManagement.getProductById(1),-1);
        if(result>0){
            System.out.println("Product update successfully!");
        }
        else{
            System.out.println("Product update failed!");
        }
    }

    @Test
    public void deleteProductByIdTest() {
        ProductManagement productManagement= new ProductManagement();
        int result=productManagement.deleteProductById(5);
        if(result>0){
            System.out.println("Product deleted successfully!");
        }
        else{
            System.out.println("Product deletion failed!");
        }
    }

    @Test
    public void selectProductsByPage(){
        ProductManagement productManagement= new ProductManagement();
        List<Product> products = new ArrayList<>();
        products=productManagement.getProductsByPage(2);
        if (products!=null){
            for (Product product : products) {
                System.out.println(product);
            }
        }
        else {
            System.out.println("Product not found!");
        }
    }



    @Test
    public void creatOrderTest() {

        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        List<Order> orders= new ArrayList<>();
        List<Item> items1=new ArrayList<>();
        List<Item> items2=new ArrayList<>();
        Collections.addAll(items1,
                new Item(1,1,productManagement.getProductById(1).getPrice()),
                new Item(2,1,productManagement.getProductById(2).getPrice()),
                new Item(3,1,productManagement.getProductById(3).getPrice())
        );
        Collections.addAll(items2,
                new Item(2,2,productManagement.getProductById(2).getPrice()),
                new Item(3,2,productManagement.getProductById(3).getPrice()),
                new Item(4,2,productManagement.getProductById(4).getPrice())
                );
        Collections.addAll(orders,
                new Order(items1),
                new Order(items2)
                );

        for (Order order : orders) {
            int result = orderManagement.insertOrder(order);
            if(result == 0) {
                System.out.println("Order "+order.getOrderID()+"inserted failed");
            }
        }
        System.out.println("All orders inserted");
    }

    @Test
    public void updateOrderTest() throws Exception{

        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        int result=orderManagement.updateOrderById(1,1,-3);

        if(result>0){
            System.out.println("Order update successfully!");
        }
        else{
            System.out.println("Order update failed!");
        }


    }



    @Test
    //订单中选了不存在的商品
    //查询不到产品 7
    //查询产品出错！
    public void creatOrderErrorTest1() {
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        List<Item> items=new ArrayList<>();
        Collections.addAll(items,
                new Item(1,1),
                new Item(2,1),
                new Item(7,1)
        );
        Order order =new Order(items);
        int result=orderManagement.insertOrder(order);
        if(result>0){
            System.out.println("Order inserted successfully!");
        }
        else{
            System.out.println("Order insertion failed!");
        }

    }

    @Test
    //订单中购买的商品数量超过库存
    //库存不足，无法购买！
    //无法更新产品 1 的库存
    public void creatOrderErrorTest2() {
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        List<Item> items=new ArrayList<>();
        Collections.addAll(items,
                new Item(1,1000000),
                new Item(2,1),
                new Item(3,1)
        );
        Order order =new Order(items);
        int result=orderManagement.insertOrder(order);
        if(result>0){
            System.out.println("Order inserted successfully!");
        }
        else{
            System.out.println("Order insertion failed!");
        }
    }

    @Test
    public void deleteOrderByIdTest() {
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        //级联删除
        int result=orderManagement.deleteOrderById(2);
        if(result>0){
            System.out.println("Order deleted successfully");
        }
        else{
            System.out.println("Order deletion failed");
        }

    }

    @Test
    public void selectOrderByIdTest() {

        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        Order order=orderManagement.getOrderById(1);
        if(order!=null){
            System.out.println(order);
        }
        else{
            System.out.println("Order not found!");
        }

    }

    @Test
    public void selectAllOrderTest(){
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        List<Order> orders=orderManagement.getAllOrdersOrderBy("order_id");;
        if(orders!=null){
            for (Order order : orders) {
                System.out.println(order.getOrderID()+" "+order.getTotalPrice());
            }
        }
        else{
            System.out.println("Order not found!");
        }
    }

    @Test
    public void selectOrderByPageTest(){

        OrderManagement orderManagement= new OrderManagement();
        List<Order> orders=orderManagement.getOrdersByPage(2);
        if(orders!=null){
            for (Order order : orders) {
                System.out.println(order.getOrderID()+" "+order.getTotalPrice());
            }
        }
        else{
            System.out.println("Order not found!");
        }

    }


}
