package com.example.domian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: recordInfo
 * @Description:录像实体-保存预览id和录像路径
 * @Author: Administrator
 * @Date: 2023年01月28日 9:24
 * @Version: 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class recordInfo {
    Integer lRealHandle;
    String recordPath;
}
