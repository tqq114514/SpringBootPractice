package com.tedu.springbootpractice.controller;

import com.tedu.springbootpractice.entity.Article;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*声明当前类处理发表文件的操作*/
@Controller
public class ArticleController {
    /*由于保存文章内容的目录只需要创建一次，则我们将其置于静态块
    * 利用FiLe创建文件目录*/
   public static File writerDir;
   static {
       writerDir = new File("./writerUsers");
       if (!writerDir.exists()){
           writerDir.mkdirs();
           /*mkdirs()方法创建该文件夹及其所有缺少的父文件夹*/
       }
   }

    @RequestMapping("/writeArticle")
    public void writeArticle(HttpServletRequest request , HttpServletResponse response){
        String title = request.getParameter("title");
        String author = request.getParameter("author");
        String content = request.getParameter("content");

        /*判断验证信息是否合法，并阻止前端的非法请求
        * 包括用户在url上随意提交不带完整参数产生的null可能引发的空指针异常
        * 表单没有写内容产生的空字符串*/
        if(title == null || title.isEmpty()||
        author == null || "".equals(author)||
        content == null || content.isEmpty()){
            try {
                response.sendRedirect("/article_fail.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;  //信息不合法，则直接结束
        }
        System.out.printf(title+author+content);

        File file = new File(writerDir,title+".obj");  //创建的文件父目录为writerDir,文件名为标题.obj
        Article article = new Article(title,author,content);

        /*文章已存在(文件名已存在)，就要求重新发表*/
        if (file.exists()) {
            try {
                response.sendRedirect("./article_fail.html");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            ) {  //自动流关闭
            oos.writeObject(article);
            response.sendRedirect("/article_success.html");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping("/articleList")
    public void articleList(HttpServletRequest request ,HttpServletResponse response){
        System.out.println("开始处理显示文章流程");
        /*该集合用于保存writerUsers目录中的文件信息，数据类型是Article类型*/
        List<Article> articlelist = new ArrayList<>();
        /*或者writerusers目录下的文件名.obj并存入File数组中*/
        File[] files = writerDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File article) {
                return article.getName().endsWith(".obj");
            }
        });
        /*利用对象流从File[]中挨个读取文件名保存到集合中*/
        for (File file :files){
            try {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                Article article = (Article) ois.readObject();
                articlelist.add(article);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        response.setContentType("text/html;charset=utf-8");
        try {
            PrintWriter pw = response.getWriter();
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"zh-cn\">");
            pw.println("<head>");
            pw.println("<meta charset=\"UTF-8\">");
            pw.println("<title>用户发表的文章</title>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<center>");
            pw.println("<h2>文章列表页</h2>");
            pw.println("<table border=\"1px\">");
            pw.println("<tr>");
            pw.println("<td>文章标题</td>");
            pw.println("<td>作者</td>");
            pw.println("<td>文章内容</td>");
            pw.println("</tr>");

            /*for(数据类型 变量名：遍历的目标)*/
            for(Article list:articlelist) {
            pw.println("<tr>");
            pw.println("<td>"+list.getTitle()+"</td>");
            pw.println("<td>"+list.getAuthor()+"</td>");
            pw.println("<td>"+list.getContent()+"</td>");
            pw.println("</tr>");
            }
            
            pw.println("</table>");
            pw.println("</center>");
            pw.println("</body>");
            pw.println("</html>");
            pw.println("");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}


