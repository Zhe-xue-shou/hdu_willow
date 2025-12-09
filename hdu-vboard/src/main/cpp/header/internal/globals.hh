#ifndef _VBOARD_GLOBALS__HH_
#define _VBOARD_GLOBALS__HH_
#include "pins.hh"
#include "pins_name.hh"
#include <vector>

// 利用结构体替换map结构
// 完成映射逻辑
struct pin_map {
    void *ptr;      // 变量地址
    std::vector<PINS> pins_vec; // 引脚列表
    unsigned long max_byte;  //变量字节数
};

const static char *pins_name[] = {
    "CLK",
    INPUT_SW_STR,
    INPUT_SWB_STR,
    INPUT_HEX_STR,
    OUTPUT_LED_STR,
    OUTPUT_DP_STR,
    OUTPUT_SEG_STR
};

// 输入输出信号列表
std::vector<pin_map> input_pins_map;
std::vector<pin_map> output_pins_map;

unsigned int pins_value[PINS_LEN] = { 0 };

#endif