package core.pipeline;

import core.engine.bean.Context;
import core.engine.bean.Spider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author: zhaoyoucheng
 * @Date: 2019/7/10 13:27
 * @Description:
 */
public interface Pipeline {

    Logger logger = LoggerFactory.getLogger(Pipeline.class);

    default void processItem(Item item, Spider spider) {

    }

    default void openSpider(Spider spider) {

    }

    default void closeSpider(Spider spider) {

    }

    default void fromCrawler(Context context) {

    }
}
