package com.qh.system.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.qh.pay.api.utils.QhPayUtil;
import com.qh.redis.RedisConstants;
import com.qh.redis.service.RedisUtil;
import com.qh.system.dao.ConfigDao;
import com.qh.system.domain.ConfigDO;
import com.qh.system.service.ConfigService;

@Service
public class ConfigServiceImpl implements ConfigService {

    @Autowired
    private ConfigDao configDao;


    @Override
    public ConfigDO get(String configItem) {
    	ConfigDO config = (ConfigDO) RedisUtil.getRedisTemplate().opsForHash().get(RedisConstants.cache_config,
    			configItem);
		if(config == null){
			config =  configDao.getByItem(configItem);
			RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_config, configItem, config);
		}
		return config;
    }

    @Override
    public List<ConfigDO> list(Map<String, Object> map) {
        return configDao.list(map);
    }

    @Override
    public int count(Map<String, Object> map) {
        return configDao.count(map);
    }

    public ConfigDO save(ConfigDO config) {
        RedisUtil.syncConfig(config, false);
        configDao.save(config);
        RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_config, config.getConfigItem(), config);
        return config;
    }

    @Override
    public ConfigDO update(ConfigDO config) {
        
        String configValue = config.getConfigValue();
    	if(config.getConfigItem().equals("privateKeyPath") || config.getConfigItem().equals("publicKeyPath")) {
    		config.setConfigValue("");
        }
    	RedisUtil.syncConfig(config, false);
        configDao.update(config);
        config.setConfigValue(configValue);
        RedisUtil.getRedisTemplate().opsForHash().put(RedisConstants.cache_config, config.getConfigItem(), config);
        if(config.getConfigItem().equals("privateKeyPath")) {
    		QhPayUtil.setQhPrivateKey(configValue);
        }else if(config.getConfigItem().equals("publicKeyPath")) {
        	QhPayUtil.setQhPublicKey(configValue);
        }else if(config.getConfigItem().equals("merchNoPrefix")) {
        	QhPayUtil.setMerchNoPrefix(configValue);
        }else if(config.getConfigItem().equals("agentNoPrefix")) {
        	QhPayUtil.setAgentNoPrefix(configValue);
        }
        return config;
    }


    /*
     * <p>Title: batchRemove</p> <p>Description: </p>
     * @param ids
     * @param configItems
     * @param parentItems
     * @see com.qh.system.service.ConfigService#batchRemove(java.lang.Integer[], java.lang.String[])
     */

    @Override
    public void batchRemove(Integer[] ids, String[] configItems, String[] parentItems) {
        int result = configDao.batchRemove(ids);
        if (result > 0) {
            for (int i = 0; i < ids.length; i++) {
            	RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_config, configItems[i]);
                RedisUtil.delConfig(configItems[i], parentItems[i]);
            }
        }
    }

    /*
     * <p>Title: remove</p> <p>Description: </p>
     * @param id
     * @param cofigItem
     * @return
     * @see com.qh.system.service.ConfigService#remove(java.lang.Integer, java.lang.String)
     */
    public int remove(Integer id, String configItem, String parentItem) {
        int result = configDao.remove(id);
        if (result > 0) {
        	RedisUtil.getRedisTemplate().opsForHash().delete(RedisConstants.cache_config, configItem);
            RedisUtil.delConfig(configItem, parentItem);
        }
        return result;
    }

    /*
     * <p>Title: exit</p> <p>Description: </p>
     * @param params
     * @return
     * @see com.qh.system.service.ConfigService#exit(java.util.Map)
     */

    @Override
    public boolean exit(Map<String, Object> params) {
        return configDao.list(params).size() > 0;
    }

}
