package com.leyou.service;

import com.leyou.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

@Service
public class GoodsHtmlService {

    @Autowired
    private TemplateEngine engine;
    @Autowired
    private GoodsService goodsService;

    private static final Logger LOGGER = LoggerFactory.getLogger(GoodsHtmlService.class);

    /**
     * 创建静态html页面
     * @param spuId
     */
    public void createHtml(Long spuId){

        //初始化运行上下文
        Context context = new Context();
        //设置数据模板
        context.setVariables(this.goodsService.loadData(spuId));

        PrintWriter printWriter = null;
        try {
            //把静态文件生成到服务器本地
            File file = new File("D:\\nginx\\html\\item\\"+spuId+".html");
            printWriter = new PrintWriter(file);

            this.engine.process("item",context,printWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    /**
     * 新建线程处理页面静态化
     * @param spuId
     */
    public void asyncExcute(Long spuId){
        ThreadUtils.execute(()->createHtml(spuId));
    }

    /**
     * 删除一个静态页面
     * @param id
     */
    public void deleteHtml(Long id) {
        File file = new File("D:\\nginx\\html\\item\\" + id + ".html");
        file.deleteOnExit();
    }
}
