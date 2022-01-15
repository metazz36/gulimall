package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.SkuSaleAttrValueDao;
import com.atguigu.gulimall.product.service.BrandService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.SkuItemSaleAttrVo;
import com.atguigu.gulimall.product.vo.SpuItemAttrGroupVo;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {

	@Autowired
	BrandService brandService;

	@Autowired
	CategoryService categoryService;

	@Autowired
	StringRedisTemplate stringRedisTemplate;

	@Autowired
	RedissonClient redissonClient;

	@Autowired
	AttrGroupDao attrGroupDao;

	@Autowired
	SkuSaleAttrValueDao skuSaleAttrValueDao;

	@Test
	public void testMybatis(){
		List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(13L);
		System.out.println(saleAttrBySpuId);

	}

	@Test
	public void testRedssion(){
		System.out.println(redissonClient);

	}

	@Test
	public void teststringRedisTemplate(){
		ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
		//保存
		ops.set("hello","world_"+ UUID.randomUUID().toString());
		//查询
		String hello = ops.get("hello");
		System.out.println("之前保存的数据是" + hello);
	}


	@Test
	public void testFindService(){
		Long[] catelogPath = categoryService.findCatelogPath(225L);
		log.info("完整路径:{}", Arrays.asList(catelogPath));
	}

//	@Autowired
//	OSSClient ossClient;

	@Test
	public void testUpload() throws FileNotFoundException {

//		// yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//		String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//		String accessKeyId = "LTAI5t7Wvd4MfEjxXfsPJKu1";
//		String accessKeySecret = "aRqusWD5pFY1mr9VobrCL1wma0Oho4";
//
//        // 创建OSSClient实例。
//		OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);



		// 填写本地文件的完整路径。如果未指定本地路径，则默认从示例程序所属项目对应本地路径中上传文件流。
//		InputStream inputStream = new FileInputStream("C:\\Users\\a1382\\Desktop\\uploadTest\\car.jpg");

//		// 依次填写Bucket名称（例如examplebucket）和Object完整路径（例如exampledir/exampleobject.txt）。Object完整路径中不能包含Bucket名称。
//		ossClient.putObject("gulimall-metazz", "car.jpg", inputStream);
//
//        // 关闭OSSClient。
//		ossClient.shutdown();
//
//		System.out.println("上传成功！");

	}

	@Test
	public void contextLoads() {
//		BrandEntity brandEntity = new BrandEntity();
//		brandEntity.setBrandId(3L);
//		brandEntity.setDescript("战狼1号");
//
////		brandEntity.setDescript("战狼");
////		brandEntity.setName("华为");
////		brandService.save(brandEntity);
////		System.out.println("保存成功...");
//
//		brandService.updateById(brandEntity);
//
//		List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 3L));
//		list.forEach((item)->{
//			System.out.println(item);
//		});
	}

}
