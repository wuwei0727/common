package com.tgy.rtls.data.service.park.impl;

import com.tgy.rtls.data.entity.park.ParkingLotCost;
import com.tgy.rtls.data.mapper.park.ParkingLotCostMapper;
import com.tgy.rtls.data.service.park.ParkingLotCostService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
/**
*@BelongsProject: rtls
*@BelongsPackage: com.tgy.rtls.data.service.park
*@Author: wuwei
*@CreateTime: 2023-11-06 11:52
*@Description: TODO
*@Version: 1.0
*/
@Service
public class ParkingLotCostServiceImpl implements ParkingLotCostService{

    @Resource
    private ParkingLotCostMapper parkingLotCostMapper;

    @Override
    public int deleteByPrimaryKey(Integer id) {
        return parkingLotCostMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(ParkingLotCost record) {
        return parkingLotCostMapper.insert(record);
    }

    @Override
    public int insertSelective(ParkingLotCost record) {
        return parkingLotCostMapper.insertSelective(record);
    }

    @Override
    public ParkingLotCost getParkLotCostById(Integer id, Integer map,String desc) {
        return parkingLotCostMapper.getParkLotCostById(id,map,desc);
    }

    @Override
    public int updateByPrimaryKeySelective(ParkingLotCost record) {
        return parkingLotCostMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(ParkingLotCost record) {
        return parkingLotCostMapper.updateByPrimaryKey(record);
    }

	@Override
	public List<ParkingLotCost> selectAllByMap(Integer map, String[] mapids){
		 return parkingLotCostMapper.selectAllByMap(map,mapids);
	}


	@Override
	public int deleteByIdIn(String[] ids){
		 return parkingLotCostMapper.deleteByIdIn(ids);
	}










}
