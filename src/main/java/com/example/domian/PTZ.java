package com.example.domian;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName: PTZ
 * @Description:
 * @Author: Administrator
 * @Date: 2023年01月18日 14:23
 * @Version: 1.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PTZ {
    String wPanPos;
    String wTiltPos;
    String wZoomPos;
}
