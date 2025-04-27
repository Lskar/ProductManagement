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
    public void insertProductTest() throws Exception {
        ProductManagement productManagement = new ProductManagement();
        try {
            List<Product> products = new ArrayList<>();
            Collections.addAll(products,
                    new Product("小狗",600,999),
                    new Product("小猫",500,999),
                    new Product("薯片",5,888),
                    new Product("可乐",3,777),
                    new Product("巧克力",10,666)
            );
            for (Product product : products) {
                productManagement.insertProduct(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("所有商品添加成功！");
    }

    @Test
    public void selectProductByIdTest() throws Exception {
        ProductManagement productManagement= new ProductManagement();
        try {
            Product product = productManagement.getProductById(1);
            System.out.println(product);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("查询成功！");
    }

    @Test
    public void selectAllProductsTest() throws Exception {
        List<Product> productList= new ArrayList<Product>();
        ProductManagement productManagement= new ProductManagement();
        try {
            productList=productManagement.getAllProducts("price");
            for (Product product : productList) {
                System.out.println(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("所有商品查询成功！");
    }

    @Test
    public void updateProductStockByIdTest() throws Exception{
        ProductManagement productManagement= new ProductManagement();
        //传入购买数量
        try {
            productManagement.updateProduct(productManagement.getProductById(1),-1);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("商品更新库存成功！");
    }

    @Test
    public void deleteProductByIdTest() throws Exception {
        ProductManagement productManagement= new ProductManagement();
        try {
            productManagement.deleteProductById(5);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("删除成功！");
    }

    @Test
    public void selectProductsByPage() throws Exception{
        ProductManagement productManagement= new ProductManagement();
        try {
            List<Product> products = new ArrayList<>();
            products=productManagement.getProductsByPage(1);
            for (Product product : products) {
                System.out.println(product);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            productManagement.closeConnection();
        }
        System.out.println("分页查询成功！");
    }


    @Test
    public void createOrderTest() throws Exception {

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
        try {
            for (Order order : orders) {
                orderManagement.insertOrder(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("All orders inserted");
    }

    @Test
    public void updateOrderTest() throws Exception{

        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        try {
            orderManagement.updateOrderById(1,1,-1);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("订单更新成功！");
    }



    @Test
    //订单中选了不存在的商品
    //查询不到产品 7
    public void creatOrderErrorTest1() throws Exception{
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        List<Item> items=new ArrayList<>();
        Collections.addAll(items,
                new Item(1,1),
                new Item(2,1),
                new Item(7,1)
        );
        Order order =new Order(items);
        try {
            orderManagement.insertOrder(order);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("All orders inserted");
    }

    @Test
    //订单中购买的商品数量超过库存
    //库存不足，无法购买！
    public void creatOrderErrorTest2() throws Exception{
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        List<Item> items=new ArrayList<>();
        Collections.addAll(items,
                new Item(1,1000000),
                new Item(2,1),
                new Item(3,1)
        );
        Order order =new Order(items);
        try {
            orderManagement.insertOrder(order);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("All orders inserted");
    }

    @Test
    public void deleteOrderByIdTest() throws Exception{
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();

        try {
            //级联删除
            int result=orderManagement.deleteOrderById(2);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("订单删除成功！");
    }

    @Test
    public void selectOrderByIdTest() throws Exception{

        OrderManagement orderManagement= new OrderManagement();
        try {
            Order order=orderManagement.getOrderById(1);

            System.out.println(order);

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
        }
        System.out.println("订单查询成功！");

    }

    @Test
    public void selectAllOrderTest()throws Exception{
        ProductManagement productManagement= new ProductManagement();
        OrderManagement orderManagement= new OrderManagement();
        try {
            List<Order> orders=orderManagement.getAllOrdersOrderBy("order_id");
            for (Order order : orders) {
                System.out.println(order.getOrderID()+" "+order.getTotalPrice());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
            productManagement.closeConnection();
        }
        System.out.println("所有订单查询成功！");
    }

    @Test
    public void selectOrderByPageTest()throws Exception{

        OrderManagement orderManagement= new OrderManagement();
        try {
            List<Order> orders=orderManagement.getOrdersByPage(2);

            for (Order order : orders) {
                System.out.println(order.getOrderID()+" "+order.getTotalPrice());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return;
        } finally {
            orderManagement.closeConnection();
        }
        System.out.println("分页查询成功！");
    }
}
