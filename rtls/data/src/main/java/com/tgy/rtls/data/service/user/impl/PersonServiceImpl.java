package com.tgy.rtls.data.service.user.impl;

import com.tgy.rtls.data.common.KafukaTopics;
import com.tgy.rtls.data.common.LocalUtil;
import com.tgy.rtls.data.common.NullUtils;
import com.tgy.rtls.data.config.ImportUsersException;
import com.tgy.rtls.data.entity.equip.InfraredOrigin;
import com.tgy.rtls.data.entity.equip.InfraredOriginCount;
import com.tgy.rtls.data.entity.equip.Tag;
import com.tgy.rtls.data.entity.es.ESInfraredOriginal;
import com.tgy.rtls.data.entity.type.Department;
import com.tgy.rtls.data.entity.type.Job;
import com.tgy.rtls.data.entity.type.Level;
import com.tgy.rtls.data.entity.type.Worktype;
import com.tgy.rtls.data.entity.user.Person;
import com.tgy.rtls.data.entity.user.PersonVO;
import com.tgy.rtls.data.entity.xianjiliang.TypeCount;
import com.tgy.rtls.data.entity.xianjiliang.TypeTime;
import com.tgy.rtls.data.mapper.equip.TagMapper;
import com.tgy.rtls.data.mapper.message.WarnRecordMapper;
import com.tgy.rtls.data.mapper.type.DepartmentMapper;
import com.tgy.rtls.data.mapper.type.JobMapper;
import com.tgy.rtls.data.mapper.type.LevelMapper;
import com.tgy.rtls.data.mapper.type.WorktypeMapper;
import com.tgy.rtls.data.mapper.user.PersonMapper;
import com.tgy.rtls.data.service.common.RedisService;
import com.tgy.rtls.data.service.user.PersonService;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author 许强
 * @Package com.tgy.rtls.data.service.impl
 * @date 2020/10/14
 */
@Service
@Transactional
public class PersonServiceImpl implements PersonService{
    @Autowired(required = false)
    private PersonMapper personMapper;
    @Autowired
    private RedisService redisService;
    @Autowired(required = false)
    private WarnRecordMapper warnRecordMapper;
    @Autowired(required = false)
    private TagMapper tagMapper;
    @Autowired(required = false)
    private WorktypeMapper worktypeMapper;
    @Autowired(required = false)
    private JobMapper jobMapper;
    @Autowired(required = false)
    private LevelMapper levelMapper;
    @Autowired(required = false)
    private DepartmentMapper departmentMapper;
    @Autowired
    LocalUtil localUtil;
    private static final DateTimeFormatter time = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public List<PersonVO> findByAll(String instanceid, Integer departmentid, Integer worktypeid, Integer jobid, Integer classid,Integer status, String keyword,Integer workorder) {
        List<PersonVO> personList=personMapper.findByAll(instanceid,departmentid,worktypeid,jobid,classid,status,keyword,workorder,localUtil.getLocale());
        return personList;
    }

    @Override
    public List<Person> findByInCoal(Integer map) {
        return personMapper.findByInCoal(map);
    }

    @Override
    public List<Person> findByInCoalPerson() {
        return personMapper.findByInCoalPerson();
    }

    @Override
    public List<Person> findByPersonOnLine(Integer map) {
        return personMapper.findByPersonOnLine(map);
    }

    @Override
    public List<Person> findByPersonOff(Integer map) {
        return personMapper.findByPersonOff(map);
    }

    @Override
    public Person findById(Integer id) {
        Person person=personMapper.findById(id,LocalUtil.get(KafukaTopics.OFFLINE));
        return person;
    }

    @Override
    public Person findByOffLine(Integer id) {
        return personMapper.findByOffLine(id,localUtil.getLocale());
    }

    @Override
    public String findByNameId(String ids) {
        String[] split=ids.split(",");
        return personMapper.findByNameId(split);
    }

    @Override
    public Person findByNum(String num) {
        return personMapper.findByNum(num);
    }

    @Override
    public Boolean addPerson(Person person) {
        //清理缓存
        if (!NullUtils.isEmpty(person.getTagid())){
            Tag tag=tagMapper.findById(person.getTagid(),localUtil.getLocale());
            redisService.remove("personnum::"+tag.getNum());
        }
        if(person.getSex()==0){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            person.setFinishTime(dateFormat.format(new Date()));
        }
        if(person.getCheckTime().trim().isEmpty()){
            person.setFinishTime(null);
        }
        return personMapper.addPerson(person)>0;
    }

    @Override
    public Boolean updatePerson(Person person) {
        //清理缓存
        if (!NullUtils.isEmpty(person.getTagid())){
            Person person1=personMapper.findById(person.getId(),LocalUtil.get(KafukaTopics.OFFLINE));
            Tag tag=tagMapper.findById(person1.getTagid(),localUtil.getLocale());
            Tag tag1=tagMapper.findById(person.getTagid(),localUtil.getLocale());
            if (!NullUtils.isEmpty(tag)) {
                redisService.remove("personnum::" + tag.getNum());
            }
            if (!NullUtils.isEmpty(tag1)) {
                redisService.remove("personnum::" + tag1.getNum());
            }

        }
        if(person.getSex()==0){
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            person.setFinishTime(dateFormat.format(new Date()));
        }
        if(person.getCheckTime()==null||person.getCheckTime().trim().isEmpty()){
            person.setFinishTime(null);
        }
        return personMapper.updatePerson(person)>0;
    }

    @Override
    public Boolean delPerson(String ids) {
        if(ids==null)
            return true;
        String[] split=ids.split(",");
        List<String> deletNum=new ArrayList<>();
        for (String id:split
        ) {
            Person person = personMapper.findById(Integer.valueOf(id), LocalUtil.get(KafukaTopics.OFFLINE));
            if(person!=null&&person.getTagName()!=null){
                deletNum.add(person.getTagName());
            }
        }
        if (personMapper.delPerson(split)>0){
            //删除人员出入井  出入分站  出入区域  报警数据记录
            personMapper.delIncoal(split);
            personMapper.delInarea(split);
            personMapper.delInsub(split);
            for (String num:deletNum
                 ) {
                redisService.remove("personnum::" + num);
            }

            warnRecordMapper.delWarnRecordSub(split);
        }
        return true;
    }

    @Override
    public Boolean delPersonInstance(Integer instanceid) {
        return personMapper.delPersonInstance(instanceid)>0;
    }

    @Override
    @Cacheable(value = "personnum",key = "#num")
    public Person findByTagNum(String num) {
        return personMapper.findByTagNum(num);
    }

    @Override
    public void updatePersonOff(Integer personid, String offTime) {
        //清理缓存
        removePersonnum(personid);
        personMapper.updatePersonOff(personid, offTime);
    }

    @Override
    public void updatePersonMine(Integer personid, Integer minestate, String time) {
        //清理缓存
        removePersonnum(personid);
        personMapper.updatePersonMine(personid,minestate,time);
    }

    @Override
    public void updatePersonMap(Integer personid, Integer map) {
        //清理缓存
        removePersonnum(personid);
        personMapper.updatePersonMap(personid, map);
    }

    @Override
    public void updatePersonSub(Integer personid, String sub, String insubTime) {
        //清理缓存
        Person person1=personMapper.findById(personid,LocalUtil.get(KafukaTopics.OFFLINE));
        if(person1==null)
            return;
        Tag tag=tagMapper.findById(person1.getTagid(),localUtil.getLocale());
        if (!NullUtils.isEmpty(tag)) {
            redisService.remove("personnum::" + tag.getNum());
        }
        personMapper.updatePersonSub(personid, sub, insubTime);
    }

    @Override
    public int findByCount(Integer map) {
        return personMapper.findByCount(map);
    }

    @Override
    public int findByOff(Integer map) {
        return personMapper.findByOff(map);
    }

    @Override
    public int findByOvertime(Integer map) {
        return personMapper.findByOvertime(map);
    }

    @Override
    public int importPersonFromExcel(MultipartFile excelFile, Integer instanceid) throws Exception {
        int count=0;
        HSSFWorkbook book=null;
        try {
            //使用poi解析Excel文件
            book=new HSSFWorkbook(excelFile.getInputStream());
            //根据名称获得指定Sheet对象
            HSSFSheet hssfSheet=book.getSheetAt(0);
            if (hssfSheet!=null){
                List<HSSFPictureData> pictures=book.getAllPictures();
                Map<Integer,HSSFPictureData> picDataMap=new HashMap<>();
                if (hssfSheet.getDrawingPatriarch()!=null){
                    for (HSSFShape shape:hssfSheet.getDrawingPatriarch().getChildren()){
                        HSSFClientAnchor anchor=(HSSFClientAnchor) shape.getAnchor();
                        if (shape instanceof HSSFPicture){
                            HSSFPicture pic=(HSSFPicture)shape;
                            int row=anchor.getRow1();
                            int pictureIndex=pic.getPictureIndex()-1;
                            try {
                                HSSFPictureData picData=pictures.get(pictureIndex);
                                picDataMap.put(row,picData);
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }
                }
                int rows=hssfSheet.getPhysicalNumberOfRows();
                if (rows<2){
                    return 0;
                }
                String[] titles={"姓名","性别","出生日期","电话","身份证","卡号","工号","工种","职务","部门","级别"};
                HSSFRow headerRow=hssfSheet.getRow(0);
                int cells=headerRow.getPhysicalNumberOfCells();
                List<String> cellList=new LinkedList<>();
                for (int i=0;i<cells;i++){
                    String cellName=headerRow.getCell(i).getStringCellValue();
                    cellList.add(cellName);
                }
                if (cellList.size()<titles.length){
                    throw new ImportUsersException("导入数据模板格式错误");
                }
                for (int i=0;i<titles.length;i++){
                    if (!titles[i].equals(cellList.get(i))){
                        throw new ImportUsersException("导入数据模板格式错误");
                    }
                }
                Map<Integer,String> nullExist=new LinkedHashMap<>();//空指针判断
                Map<Integer,String> tagExist=new LinkedHashMap<>();//卡号异常
                Map<Integer,String> numExist=new LinkedHashMap<>();//工号异常
                Map<Integer,String> worktypeExist=new LinkedHashMap<>();//工种异常
                Map<Integer,String> jobExist=new LinkedHashMap<>();//职务异常
                Map<Integer,String> departmentExist=new LinkedHashMap<>();//部门异常
                Map<Integer,String> levelExist=new LinkedHashMap<>();//级别异常
                for (int i=1;i<rows;i++){
                    HSSFRow row=hssfSheet.getRow(i);
                    if (row!=null){
                        for (int j=0;j<cells;j++){
                            if (row.getCell(j)!=null)
                                row.getCell(j).setCellType(CellType.STRING);
                        }
                        String[] val=new String[11];
                        for (int j=0;j<cells;j++){
                            HSSFCell cell=row.getCell(j);
                            if (cell!=null){
                                switch (cell.getCellTypeEnum()){
                                    case FORMULA:
                                        break;
                                    case NUMERIC:
                                        val[j]= String.valueOf(cell.getNumericCellValue());
                                        break;
                                    case STRING:
                                        val[j]=cell.getStringCellValue();
                                        break;
                                    default:
                                        val[j]="";
                                }
                            }
                        }
                        try {
                            Person person=new Person();
                            //1.姓名非空判断
                            if (!NullUtils.isEmpty(val[0])) {
                                person.setName(val[0]);
                            }else {
                                nullExist.put(i,val[0]);
                                continue;
                            }
                            //2.性别判断 默认0
                            int sex=1;
                            if (!NullUtils.isEmpty(val[1])&&val[1].equals("女")) {
                                   sex=0;
                            }
                            person.setSex(sex);
                            //3.出生日期
                            person.setBirthday(val[2]);
                            //4.电话
                            person.setPhone(val[3]);
                            //5.身份证
                            person.setIdentity(val[4]);
                            //6.卡号
                            if (!NullUtils.isEmpty(val[5])) {
                                Tag tag = tagMapper.findByNum(val[5]);
                                Person person2 = personMapper.findByTagNum(val[5]);
                                if (!NullUtils.isEmpty(tag) && NullUtils.isEmpty(person2)) {//该卡号存在且没有绑定人员
                                    person.setTagid(tag.getId());
                                } else {
                                    tagExist.put(i, val[5]);
                                    continue;
                                }
                            }
                            //7.工号
                            Person person1=personMapper.findByNum(val[6]);
                            if (!NullUtils.isEmpty(person1)){//工号已存在
                                numExist.put(i,val[6]);
                                continue;
                            }else{
                                person.setNum(val[6]);
                            }
                            //8.工种
                            if (!NullUtils.isEmpty(val[7])) {
                                List<Worktype> worktype = worktypeMapper.findByName(instanceid, val[7]);
                                if (NullUtils.isEmpty(worktype)) {//如果该工种未存在 则异常
                                    worktypeExist.put(i, val[7]);
                                    continue;
                                } else {
                                    person.setWorktype(worktype.get(0).getId());
                                }
                            }
                            //9.职务
                            if (!NullUtils.isEmpty(val[8])) {
                                List<Job> job = jobMapper.findByName(instanceid, val[8]);
                                if (NullUtils.isEmpty(job)){
                                    jobExist.put(i,val[8]);
                                    continue;
                                }else {
                                    person.setJob(job.get(0).getId());
                                }
                            }
                            //10.部门
                            if (!NullUtils.isEmpty(val[9])) {
                                List<Department> department = departmentMapper.findByName(instanceid, val[9]);
                                if (NullUtils.isEmpty(department)){
                                    departmentExist.put(i,val[9]);
                                    continue;
                                }else {
                                    person.setDepartment(department.get(0).getId());
                                }
                            }
                            //11.级别
                            if(!NullUtils.isEmpty(val[10])){
                                List<Level> level=levelMapper.findByName(instanceid,val[10]);
                                if (NullUtils.isEmpty(level)){
                                    levelExist.put(i,val[10]);
                                    continue;
                                }else {
                                    person.setLevel(level.get(0).getId());
                                }
                            }
                            person.setInstanceid(instanceid);
                            if (personMapper.addPerson(person)>0){
                                count++;
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                }
                int failNum=tagExist.size()+numExist.size()+worktypeExist.size()+departmentExist.size()+levelExist.size()+jobExist.size()+nullExist.size();
                if (failNum>0) {
                    String errorStr = count + "条数据导入成功," + failNum + "条数据导入失败。<br/>";
                    if (nullExist.size() > 0) {
                        errorStr += nullExist.size() + "条信息填写不完整(不能为空):<br/>";
                        Integer[] keys = new Integer[nullExist.size()];
                        nullExist.keySet().toArray(keys);
                        for (int key : keys) {
                            String id = nullExist.get(key);
                            errorStr += "第" + key + "行：" + id + "<br/>";
                        }
                    }
                    if (tagExist.size()>0){
                        errorStr+=tagExist.size()+"条信息的卡号已绑定人员或未添加:<br/>";
                        Integer[] keys=new Integer[tagExist.size()];
                        tagExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=tagExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    if (numExist.size()>0){
                        errorStr+=numExist.size()+"条信息的工号已存在:<br/>";
                        Integer[] keys=new Integer[numExist.size()];
                        numExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=numExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    if (worktypeExist.size()>0){
                        errorStr+=worktypeExist.size()+"条信息的工种不存在:<br/>";
                        Integer[] keys=new Integer[worktypeExist.size()];
                        worktypeExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=worktypeExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    if (departmentExist.size()>0){
                        errorStr+=departmentExist.size()+"条信息的部门不存在:<br/>";
                        Integer[] keys=new Integer[departmentExist.size()];
                        departmentExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=departmentExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    if (jobExist.size()>0){
                        errorStr+=jobExist.size()+"条信息的职务不存在:<br/>";
                        Integer[] keys=new Integer[jobExist.size()];
                        jobExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=jobExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    if (levelExist.size()>0){
                        errorStr+=levelExist.size()+"条信息的等级不存在:<br/>";
                        Integer[] keys=new Integer[levelExist.size()];
                        levelExist.keySet().toArray(keys);
                        for (int key:keys){
                            String id=levelExist.get(key);
                            errorStr+="第"+key+"行："+id+"<br/>";
                        }
                    }
                    throw new ImportUsersException(errorStr);
                }
            }
        }catch (Exception e){
            throw e;
        }
        return count;
    }

    @Override
    public void exportPerson(ServletOutputStream out, String instanceid,String keyword, Integer departmentid, Integer worktypeid,
                                Integer jobid, Integer status, String title) throws Exception {
        HSSFWorkbook workbook = null;
        try {

            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet("sheet1");
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
          //  String[] titles = {"序号", "姓名", "工号", "卡号", "状态", "工种", "职务","级别","部门"};
            String[] titles = new String[9];
            titles[0]= LocalUtil.get(KafukaTopics.ID);
            titles[1]= LocalUtil.get(KafukaTopics.NAME);
            titles[2]= LocalUtil.get(KafukaTopics.NUM);
            titles[3]= LocalUtil.get(KafukaTopics.CARDNUM);
            titles[4]= LocalUtil.get(KafukaTopics.STATE);
            titles[5]= LocalUtil.get(KafukaTopics.WORKTYPE);
            titles[6]= LocalUtil.get(KafukaTopics.JOB);
            titles[7]= LocalUtil.get(KafukaTopics.LEVEL);
            titles[8]= LocalUtil.get(KafukaTopics.DEPARTMENT);

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 2000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 2000);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 2000);
            hssfSheet.setColumnWidth(6, 2000);
            hssfSheet.setColumnWidth(7, 2000);
            hssfSheet.setColumnWidth(8, 2000);
            //写入实体数据
            List<PersonVO> personList=personMapper.findByAll(instanceid,departmentid,worktypeid,jobid,null,status,keyword,null,localUtil.getLocale());
            if (!NullUtils.isEmpty(personList)){
                for (int i=0;i<personList.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    PersonVO personVO=personList.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(personVO.getName());
                    hssfRow.createCell(2).setCellValue(personVO.getNum());
                    hssfRow.createCell(3).setCellValue(personVO.getTagName());
                    hssfRow.createCell(4).setCellValue(personVO.getStatusName());
                    hssfRow.createCell(5).setCellValue(personVO.getWorktypeName());
                    hssfRow.createCell(6).setCellValue(personVO.getJobName());
                    hssfRow.createCell(7).setCellValue(personVO.getLevelName());
                    hssfRow.createCell(8).setCellValue(personVO.getDepartmentName());
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();
            throw new Exception(LocalUtil.get(KafukaTopics.EXPORT_FAIL));
        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
   public void exportInfreredCount(ServletOutputStream out, Map map) throws IOException {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            Iterator<Map.Entry<Integer,List>> iterator=map.entrySet().iterator();
            while(iterator.hasNext()){
                Map.Entry<Integer,List> entry=iterator.next();
                System.out.println(entry.getKey());
                System.out.println(entry.getValue());
               List list= entry.getValue();
                //在webbook中添加一个sheet,对应Excel文件中的sheet
                HSSFSheet hssfSheet = workbook.createSheet(entry.getKey()+"");
                //在sheet中添加表头第0行 查询数据的条件
                HSSFRow hssfRowTitle = hssfSheet.createRow(0);
                hssfRowTitle.createCell(0).setCellValue("date Loss rate");
                /*
                 * 合并单元格
                 * */
                CellRangeAddress region=new CellRangeAddress(0,0,0,8);
                hssfSheet.addMergedRegion(region);
                //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
                HSSFRow hssfRow = hssfSheet.createRow(1);
                //创建单元格，并设置值表头 设置表头居中
                HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
                //居中样式
                //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                //  String[] titles = {"序号", "姓名", "工号", "卡号", "状态", "工种", "职务","级别","部门"};
                String[] titles = new String[9];
                titles[0]= "序号";
                titles[1]="网关";
                titles[2]= "应收总包数";
                titles[3]="实收总包数";
                titles[4]= "丢时间戳";
                titles[5]="丢包率";
                titles[6]="丢时间戳率";
                titles[7]="丢时间戳统计信息";
                HSSFCell hssfCell = null;
                for (int i = 0; i < titles.length; i++) {
                    hssfCell = hssfRow.createCell(i);//列索引从0开始
                    hssfCell.setCellValue(titles[i]);//列名
                    hssfCell.setCellStyle(hssfCellStyle);
                }
                hssfSheet.setColumnWidth(0, 2000);
                hssfSheet.setColumnWidth(1, 4000);
                hssfSheet.setColumnWidth(2, 2000);
                hssfSheet.setColumnWidth(3, 2000);
                hssfSheet.setColumnWidth(4, 2000);
                hssfSheet.setColumnWidth(5, 2000);
                hssfSheet.setColumnWidth(6, 2000);
                hssfSheet.setColumnWidth(7, 8000);

                //写入实体数据
                //   List<PersonVO> personList=personMapper.findByAll(instanceid,departmentid,worktypeid,jobid,null,status,keyword,null,localUtil.getLocale());
                if (!NullUtils.isEmpty(list)){
                    for (int i=0;i<list.size();i++){
                        int rownum=i+2;
                        hssfRow=hssfSheet.createRow(rownum);
                        InfraredOriginCount infraredOriginCount=(InfraredOriginCount) list.get(i);
                        //创建单元格，并设置值
                        hssfRow.createCell(0).setCellValue(i+1);
                        hssfRow.createCell(1).setCellValue(infraredOriginCount.getGatewaynum());
                        hssfRow.createCell(2).setCellValue(infraredOriginCount.getTotal());
                        hssfRow.createCell(3).setCellValue(infraredOriginCount.getTotal_receive());
                        hssfRow.createCell(4).setCellValue(infraredOriginCount.getTotal_back());
                        hssfRow.createCell(5).setCellValue(1-infraredOriginCount.getTotal_receive()/(double)infraredOriginCount.getTotal());
                        hssfRow.createCell(6).setCellValue(infraredOriginCount.getTotal_back()*4/(double)infraredOriginCount.getTotal());
                        hssfRow.createCell(7).setCellValue(infraredOriginCount.getBack_detail());
                    //    hssfRow.createCell(8).setCellValue(personVO.getOt()==null?"":personVO.getOt().intValue()+"");
                    }
                }

            }
            workbook.write(out);

            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();

        } finally {
            if(workbook != null)
                workbook.close();
        }
    }


    @Override
    public void exportInfreredCount2(ServletOutputStream out, List<ESInfraredOriginal> esInfraredOriginals) throws IOException {
        HSSFWorkbook workbook = null;
        try {
            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            for (ESInfraredOriginal original : esInfraredOriginals) {
                //在webbook中添加一个sheet,对应Excel文件中的sheet
                HSSFSheet hssfSheet = workbook.createSheet(original.getInfraredNum());
                //在sheet中添加表头第0行 查询数据的条件
                HSSFRow hssfRowTitle = hssfSheet.createRow(0);
                hssfRowTitle.createCell(0).setCellValue("date Loss rate");
                /*
                 * 合并单元格
                 * */
                CellRangeAddress region=new CellRangeAddress(0,0,0,8);
                hssfSheet.addMergedRegion(region);
                //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
                HSSFRow hssfRow = hssfSheet.createRow(1);
                //创建单元格，并设置值表头 设置表头居中
                HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
                //居中样式
                //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
                //  String[] titles = {"序号", "姓名", "工号", "卡号", "状态", "工种", "职务","级别","部门"};
                String[] titles = new String[9];
                titles[0]= "序号";
                titles[1]="网关";
                titles[2]= "应收总包数";
                titles[3]="实收总包数";
                titles[4]= "丢时间戳";
                titles[5]="丢包率";
                titles[6]="丢时间戳率";
                titles[7]="丢时间戳统计信息";
                HSSFCell hssfCell = null;
                for (int i = 0; i < titles.length; i++) {
                    hssfCell = hssfRow.createCell(i);//列索引从0开始
                    hssfCell.setCellValue(titles[i]);//列名
                    hssfCell.setCellStyle(hssfCellStyle);
                }
                hssfSheet.setColumnWidth(0, 2000);
                hssfSheet.setColumnWidth(1, 4000);
                hssfSheet.setColumnWidth(2, 2000);
                hssfSheet.setColumnWidth(3, 2000);
                hssfSheet.setColumnWidth(4, 2000);
                hssfSheet.setColumnWidth(5, 2000);
                hssfSheet.setColumnWidth(6, 2000);
                hssfSheet.setColumnWidth(7, 8000);

                //写入实体数据
                if (!NullUtils.isEmpty(esInfraredOriginals)){
                    for (int i=0;i<esInfraredOriginals.size();i++){
                        int rownum=i+2;
                        hssfRow=hssfSheet.createRow(rownum);
                        ESInfraredOriginal infraredOriginCount= esInfraredOriginals.get(i);
                        //创建单元格，并设置值
                        hssfRow.createCell(0).setCellValue(i+1);
                        hssfRow.createCell(1).setCellValue(infraredOriginCount.getGatewayNum());
                        hssfRow.createCell(2).setCellValue(infraredOriginCount.getTotal());
                        hssfRow.createCell(3).setCellValue(infraredOriginCount.getTotal_receive());
                        hssfRow.createCell(4).setCellValue(infraredOriginCount.getTotal_back());
                        hssfRow.createCell(5).setCellValue(1-infraredOriginCount.getTotal_receive()/(double)infraredOriginCount.getTotal());
                        hssfRow.createCell(6).setCellValue(infraredOriginCount.getTotal_back()*4/(double)infraredOriginCount.getTotal());
                        hssfRow.createCell(7).setCellValue(infraredOriginCount.getBack_detail());
                        //    hssfRow.createCell(8).setCellValue(personVO.getOt()==null?"":personVO.getOt().intValue()+"");
                    }
                }

            }
            workbook.write(out);

            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();

        } finally {
            if(workbook != null)
                workbook.close();
        }
    }




    @Override
    public void exportInfrered(ServletOutputStream out, List<InfraredOrigin> list,String title) throws IOException {
        HSSFWorkbook workbook = null;
        try {

            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet(title);
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //  String[] titles = {"序号", "姓名", "工号", "卡号", "状态", "工种", "职务","级别","部门"};
            String[] titles = new String[9];
            titles[0]= "序号";
            titles[1]= "时间";
            titles[2]="网关";
            titles[3]= "车位检测器";
            titles[4]="时间戳";
            titles[5]= "出现次数";
            titles[6]="状态";
            titles[7]="信号强度";
            titles[8]= "时间差(秒)";

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 4000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 2000);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 2000);
            hssfSheet.setColumnWidth(6, 2000);
            hssfSheet.setColumnWidth(7, 2000);
            hssfSheet.setColumnWidth(8, 2000);
            //写入实体数据
            //   List<PersonVO> personList=personMapper.findByAll(instanceid,departmentid,worktypeid,jobid,null,status,keyword,null,localUtil.getLocale());
            if (!NullUtils.isEmpty(list)){
                for (int i=0;i<list.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    InfraredOrigin personVO=list.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(personVO.getTimestamp().format(time));
                    hssfRow.createCell(2).setCellValue(personVO.getGatewaynum());
                    hssfRow.createCell(3).setCellValue(personVO.getInfrarednum());
                    hssfRow.createCell(4).setCellValue(personVO.getCount());
                    hssfRow.createCell(5).setCellValue(personVO.getTime_count());
                    hssfRow.createCell(6).setCellValue(personVO.getState());
                    hssfRow.createCell(7).setCellValue(personVO.getRssi());
                    hssfRow.createCell(8).setCellValue(personVO.getOt()==null?"":personVO.getOt().intValue()+"");
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();

        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public void exportInfrered2(ServletOutputStream out, List<ESInfraredOriginal> list,String title) throws IOException {
        HSSFWorkbook workbook = null;
        try {

            //创建一个workbook，对应一个Excel文件
            workbook = new HSSFWorkbook();
            //在webbook中添加一个sheet,对应Excel文件中的sheet
            HSSFSheet hssfSheet = workbook.createSheet(title);
            //在sheet中添加表头第0行 查询数据的条件
            HSSFRow hssfRowTitle = hssfSheet.createRow(0);
            hssfRowTitle.createCell(0).setCellValue(title);
            /*
             * 合并单元格
             * */
            CellRangeAddress region=new CellRangeAddress(0,0,0,8);
            hssfSheet.addMergedRegion(region);
            //在sheet中添加表头第0行,注意老版本poi对Excel的行数列数有限制short
            HSSFRow hssfRow = hssfSheet.createRow(1);
            //创建单元格，并设置值表头 设置表头居中
            HSSFCellStyle hssfCellStyle = workbook.createCellStyle();
            //居中样式
            //hssfCellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //  String[] titles = {"序号", "姓名", "工号", "卡号", "状态", "工种", "职务","级别","部门"};
            String[] titles = new String[9];
            titles[0]= "序号";
            titles[1]= "时间";
            titles[2]="网关";
            titles[3]= "车位检测器";
            titles[4]="时间戳";
            titles[5]= "出现次数";
            titles[6]="状态";
            titles[7]="信号强度";
            titles[8]= "时间差(秒)";

            HSSFCell hssfCell = null;
            for (int i = 0; i < titles.length; i++) {
                hssfCell = hssfRow.createCell(i);//列索引从0开始
                hssfCell.setCellValue(titles[i]);//列名
                hssfCell.setCellStyle(hssfCellStyle);
            }
            hssfSheet.setColumnWidth(0, 2000);
            hssfSheet.setColumnWidth(1, 4000);
            hssfSheet.setColumnWidth(2, 2000);
            hssfSheet.setColumnWidth(3, 2000);
            hssfSheet.setColumnWidth(4, 2000);
            hssfSheet.setColumnWidth(5, 2000);
            hssfSheet.setColumnWidth(6, 2000);
            hssfSheet.setColumnWidth(7, 2000);
            hssfSheet.setColumnWidth(8, 2000);
            //写入实体数据
            if (!NullUtils.isEmpty(list)){
                for (int i=0;i<list.size();i++){
                    int rownum=i+2;
                    hssfRow=hssfSheet.createRow(rownum);
                    ESInfraredOriginal personVO=list.get(i);
                    //创建单元格，并设置值
                    hssfRow.createCell(0).setCellValue(i+1);
                    hssfRow.createCell(1).setCellValue(personVO.getTimestamp());
                    hssfRow.createCell(2).setCellValue(personVO.getGatewayNum());
                    hssfRow.createCell(3).setCellValue(personVO.getInfraredNum());
                    hssfRow.createCell(4).setCellValue(personVO.getCount());
                    hssfRow.createCell(5).setCellValue(personVO.getTime_count());
                    hssfRow.createCell(6).setCellValue(personVO.getState());
                    hssfRow.createCell(7).setCellValue(personVO.getRssi());
                    hssfRow.createCell(8).setCellValue(personVO.getOt()==null?"":personVO.getOt().intValue()+"");
                }
            }
            workbook.write(out);
            out.flush();
            out.close();
        }catch(Exception e){
            e.printStackTrace();

        } finally {
            if(workbook != null)
                workbook.close();
        }
    }

    @Override
    public List<Person> findAllPerson() {
        return personMapper.findAllPerson();
    }

    @Override
    public  List<TypeTime> findTypeTimeByTypeid(Integer instanceid,Integer typeId) {
        return personMapper.findTypeTimeByTypeid(instanceid,typeId);
    }

    @Override
    public List<TypeCount> findTypeCountByTypeid(Integer instanceid, Integer typeId) {
        return personMapper.findTypeCountByTypeid(instanceid,typeId);
    }

    private void removePersonnum(Integer personid){
        //清理缓存
        Person person1=personMapper.findById(personid,LocalUtil.get(KafukaTopics.OFFLINE));
        Tag tag=tagMapper.findById(person1.getTagid(),localUtil.getLocale());
        if (!NullUtils.isEmpty(tag)) {
            redisService.remove("personnum::" + tag.getNum());
            System.out.println("remove personnum::" + tag.getNum());
        }
    }
}
