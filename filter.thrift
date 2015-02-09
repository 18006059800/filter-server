namespace java com.zyx.filter.thrift
namespace php com.zyx.filter.thrift

/** 
 * 关键字过滤行为类型 
 */
enum TBehaviorType
{	
	/** 不改变 */
	NONE = 0;					
	/** 替换 */	
	REPLACE = 1,    		
	/** 移除 */		
	REMOVE = 2,   					

}

/**
 * 关键字过滤级别
 */
enum TFilterLevel
{	
	/** 关键字出现，就根据TBehaviorType做相应的调整（下面更高的过滤级别也做调整） */
	ADJUST = 1,				
	/** 关键字出现，就标记可疑，但不限制发表 */		
	NOTICE = 2,					
	/** 关键字出现，就标记为待审核 */	
	CHECK = 3,						
	/** 禁止发表 */
	FORBIDDEN = 4					

}

/**
 * 关键字
 */
struct TFilterKeyword
{
	1 : optional string _id			

	/** 是否是正则表达式 */
	5 : optional bool isRegular										
	/** 关键字 */
	11 : required string keyword		
	/** 过滤行为类型 */							
	12 : optional TBehaviorType behavior_type = TBehaviorType.NONE 	
	/** 如果behavior_type==REPLACE,该字段代表代替的内容，否则无用 */
	13 : optional string replacement							
	/** 关键字过滤级别 */	
	14 : optional TFilterLevel level = TFilterLevel.ADJUST			
	/** 如果该字段不为0：则当该关键字出现了appear_times_limit次时，激活过滤级别，否则认为是ADJUST */
	15 : optional i32 appear_times_limit							
	/** 相当于外键 */
	31 : optional string filter_id									
}

/**
 * 时间单位
 */
enum TTimeUnit
{

	MILLISECONDS = 1,

    SECONDS = 2,

    MINUTES = 3,

    HOURS = 4,

    DAYS = 5,
    
    // 以下三个是额外增加的
    WEEKS = 6, 
    
    MONTHS = 7, // 不支持toMillis
    
    YEARS = 8;  // 不支持toMillis
    
}

/**
 * 过滤类型
 */
enum TFilterType
{
	/** 文本过滤 */
	TEXT = 1, 			
	/** 频率过滤 */
	FREQUENCY = 2		
}

/**
 * 频率过滤器
 */
struct TFrequencyFilter
{
	/** 时限（量） */
	11 : required i64 duration				
	/** 时限（单位）*/
	12 : required TTimeUnit time_unit		
	/** 时限内能出现的次数 */
	13 : required i32 times					
	/** 是否按自然期处理，比如时间单位为“天”时，isNature=true 表示按自然天过滤，
     * 每天都从0点到24点算 isNatrue=false
	 * 表示从发表内容一刻算法，到第二天同一时候 */
	14 : optional bool is_natrue			
}

/**
 * 文本过滤器
 */
struct TTextFilter
{
	/** 过滤关键字，不入库 */
	11 : optional list<TFilterKeyword> filter_keywords  
}

/**
 * 业务
 */
struct TBusiness 
{
	1 : optional string _id

	/** 业务名称，不能重复 */
	2 : required string name    		
}

struct TFilter
{
	1 : optional string _id
	/** 过滤器名称 */
	4 : required string name 						
	/** 过滤的字段 */
	5 : optional string field      
	/** 对应的业务id */           			
	6 : optional string business_id                     
	/** 对应的业务名称 */
	7 : optional string business_name			
	11 : optional TFilterType type
	12 : optional TTextFilter textFilter
	13 : optional TFrequencyFilter frequencyFilter
}

/**
 * 过滤请求
 */
struct FilterRequest
{
	/** 业务名称 */
	5 : optional string business_name						
	/** key : 字段名   value : 字段值 */
	11 : required map<string, string> filterField			
	/** key : 字段名   value : 对应的过滤器 */
	12 : optional map<string, TFilter> filterMap			 
}

/**
 * 判定结果
 */
enum TJudgeResult
{
	/** 正常发表 */
	OK = 0,				
	/** 部分关键字被调整后发表 */
	ADJUST = 1,			
	/** 发表并提醒管理员检查(发表内容也会被调整) */
	NOTICE = 2,			
	/** 待审核(发表内容也会被调整) */
	CHECK = 3,			
	/** 拒绝发表 */
	FORBIDDEN = 4		
}

/**
 * 过滤结果
 */
struct FilterResponse
{
	11 : required TJudgeResult judgeResult
	12 : required map<string, string> resultMap
}

/**
 * 返回
 */
struct TResponse
{
	/** 状态码，为0时代表正常 */
	11 : i32 code              
	/** 消息 */
	12 : string message        
}

// -------------------------定义服务---------------------------
service FilterService
{

	/**
 	 * 过滤
 	 */
	FilterResponse filter(1:FilterRequest req)
	
	/**
 	 * 增加关键字
 	 */
	TResponse addKeyword(1:TFilterKeyword keyword) 
	
	/**
 	 * 删除关键字
 	 */
	TResponse deleteKeyword(1:string _id) 
	
	/**
 	 * 更新关键字
 	 */
	TResponse updateKeyword(1:TFilterKeyword keyword) 
	
	/**
 	 * 获取关键字
 	 */
	TFilterKeyword getKeyword(1:string _id) 

	/**
 	 * 添加一个业务
 	 */
	TResponse addBusiness(1:TBusiness business)

	/**
 	 * 删除一个业务
 	 */
	TResponse deleteBusiness(1:string _id)

	/**
 	 * 更新业务
 	 */
	TResponse updateBusiness(1:TBusiness business)

	/**
 	 * 获取业务信息
 	 */
	TBusiness getBusiness(1:string _id)
	
	/**
 	 * 增加过滤器
 	 */
	TResponse addFilter(1:TFilter filter) 
	
	/**
 	 * 删除过滤器
 	 */
	TResponse deleteFilter(1:string _id) 
	
	/**
 	 * 更新过滤器
 	 */
	TResponse updateFilter(1:TFilter filter) 
	
	 // 
	/**
 	 * 获取过滤器
 	 */
	TFilter getFilter(1:string _id) 

	/**
 	 * 获取所有的过滤器
 	 */
	list<TFilter> listFilters()
	
}

