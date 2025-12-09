#ifndef FPGA__HH_
#define FPGA__HH_

#include <map>
#include <vector>
#include <string>
#include <nlohmann/json.hpp>
#include <iostream>

#define INPUT_SW    SW00, SW01, SW02, SW03, SW04, SW05, SW06, SW07, \
                    SW08, SW09, SW10, SW11, SW12, SW13, SW14, SW15, \
                    SW16, SW17, SW18, SW19

#define INPUT_SWB   SWB00, SWB01, SWB02, SWB03, SWB04, SWB05, SWB06, SWB07, SWB08, SWB09

#define INPUT_HEX   INPUT0, INPUT1, INPUT2, INPUT3

#define OUTPUT_LED  L00, L01, L02, L03, L04, L05, L06, L07, \
                    L08, L09, L10, L11, L12, L13, L14, L15, \
                    L16, L17, L18, L19

#define OUTPUT_DP   DP00, DP01, DP02, DP03, DP04, DP05

#define OUTPUT_SEG  OUTPUT00, OUTPUT01, OUTPUT02, OUTPUT03, OUTPUT04, OUTPUT05

#define INPUT_SW_STR    "SW00", "SW01", "SW02", "SW03", "SW04", "SW05", "SW06", "SW07", \
                        "SW08", "SW09", "SW10", "SW11", "SW12", "SW13", "SW14", "SW15", \
                        "SW16", "SW17", "SW18", "SW19"

#define INPUT_SWB_STR   "SWB00", "SWB01", "SWB02", "SWB03", "SWB04", "SWB05", "SWB06", "SWB07", "SWB08", "SWB09"

#define INPUT_HEX_STR   "INPUT0", "INPUT1", "INPUT2", "INPUT3"

#define OUTPUT_LED_STR  "L00", "L01", "L02", "L03", "L04", "L05", "L06", "L07", \
                        "L08", "L09", "L10", "L11", "L12", "L13", "L14", "L15", \
                        "L16", "L17", "L18", "L19"

#define OUTPUT_DP_STR   "DP00", "DP01", "DP02", "DP03", "DP04", "DP05"

#define OUTPUT_SEG_STR  "OUTPUT00", "OUTPUT01", "OUTPUT02", "OUTPUT03", "OUTPUT04", "OUTPUT05"

// 输入输出框位数
#define INPUT_HEX_MAX_BITS 32
#define DP_MAX_BITS 8
#define OUTPUT_HEX_MAX_BITS 32
#define BITS_PER_HEX 4

enum PINS {
    CLK,
    INPUT_SW,
    INPUT_SWB,
    INPUT_HEX,

    OUTPUT_LED,
    OUTPUT_DP,
    OUTPUT_SEG,
    PINS_LEN
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

// 利用结构体替换map结构
// 完成映射逻辑
struct pin_map {
    void *ptr;      // 变量地址
    std::vector<PINS> pins_vec; // 引脚列表
    unsigned long max_byte;  //变量字节数
};

// 输入输出信号列表
std::vector<pin_map> input_pins_map;
std::vector<pin_map> output_pins_map;

unsigned int pins_value[PINS_LEN] = { 0 };


// 置位函数（已内联）
#if 0
void set_bit(void *ptr, int bit_offset, int value) {
    uint8_t *byte_ptr = static_cast<uint8_t *>(ptr); // 将 void* 转换为字节指针

    // 计算目标字节的位置和位的偏移量
    int byte_index = bit_offset / 8;  // 目标字节索引
    int bit_index = bit_offset % 8;  // 目标字节内的位索引

    // 修改对应的位
    if (value) {
        byte_ptr[byte_index] |= (1 << bit_index); // 设置该位为 1
    } else {
        byte_ptr[byte_index] &= ~(1 << bit_index); // 清除该位为 0
    }
}
#endif

// 十六进制INPUT单独处理（已内联）
#if 0
void update_hex_signal_from_json(const nlohmann::json &input_json, void *sgl_ptr, PINS pin) {
    string hex_str = input_json[pins_name[pin]];
    int hex = std::stoi(hex_str, nullptr, 16);

    uint8_t *byte_ptr = static_cast<uint8_t *>(sgl_ptr);
    int byte_idx = 0;
    int bit_idx = 0;
    while (hex) {
        if (hex & 1)
            byte_ptr[byte_idx] |= (1 << bit_idx);
        else
            byte_ptr[byte_idx] &= ~(1 << bit_idx);
        hex >>= 1;
        ++bit_idx;
        byte_idx += bit_idx / 8;
        bit_idx %= 8;
    }

}
#endif

// 从json中读取并更新信号
void update_input_signals_from_json(const nlohmann::json &input_json) {
    for (auto &pin : input_pins_map) {
        uint8_t *byte_ptr = static_cast<uint8_t *>(pin.ptr);
        int byte_idx = 0;
        int bit_idx = 0;
        for (auto p = pin.pins_vec.begin();p != pin.pins_vec.end();p++) {
            const char *pin_name = pins_name[*p];
            if (!input_json.contains(pin_name)) {
                int add_bits;
                if (*p >= INPUT0 && *p <= INPUT3) { // INPUT类型自增MAX位
                    add_bits = INPUT_HEX_MAX_BITS;
                } else {
                    add_bits = 1;
                }
                bit_idx += add_bits;
                byte_idx += bit_idx / 8;
                bit_idx %= 8;
                continue;
            }
            if (byte_idx >= pin.max_byte) { //超过最大位数，中断
                break;
            }
            if (*p >= INPUT0 && *p <= INPUT3) {   // INPUT类型单独处理
                std::string hex_str = input_json[pin_name];
                unsigned int hex_num = std::stoul(hex_str, nullptr, 16);
                while (hex_num && byte_idx < pin.max_byte) {    // 内循环可能会超 要判断
                    if (hex_num & 1)
                        byte_ptr[byte_idx] |= (1 << bit_idx);
                    else
                        byte_ptr[byte_idx] &= ~(1 << bit_idx);
                    hex_num >>= 1;
                    ++bit_idx;
                    byte_idx += bit_idx / 8;
                    bit_idx %= 8;
                }
            } else {
                if (input_json[pin_name] != 0) {
                    byte_ptr[byte_idx] |= (1 << bit_idx);
                } else {
                    byte_ptr[byte_idx] &= ~(1 << bit_idx);
                }
                ++bit_idx;
                byte_idx += bit_idx / 8;
                bit_idx %= 8;
            }
        }
    }
}

// 需要自己在模块中实现
// 已重构
#if 0
void update_input_signals_from_json(const nlohmann::json &input_json);
#endif

// 更新变量信号
// 理论上只需要更新输出变量即可
int update_output_signals() {
    int change_flag = 0;
    for (auto &pin : output_pins_map) {
        uint8_t *byte_ptr = static_cast<uint8_t *>(pin.ptr);
        int byte_idx = 0;
        int bit_idx = 0;
        for (auto p = pin.pins_vec.begin();p != pin.pins_vec.end();p++) {
            if (byte_idx >= pin.max_byte)  break;
            if (*p >= OUTPUT00 && *p <= OUTPUT03) {
                unsigned int new_value = 0;
                // 内循环会超， 需要判断
                for (int i = 0;i < OUTPUT_HEX_MAX_BITS && byte_idx < pin.max_byte;i++) {
                    new_value = new_value | (((byte_ptr[byte_idx] >> bit_idx) & 1) << i);
                    ++bit_idx;
                    byte_idx += bit_idx / 8;
                    bit_idx %= 8;
                }
                change_flag = change_flag || (new_value ^ pins_value[*p]);
                pins_value[*p] = new_value;
            } else if (*p >= DP00 && *p <= DP05) {
                unsigned int new_value = 0;
                for (int i = 0;i < DP_MAX_BITS && byte_idx < pin.max_byte;i++) {
                    new_value = new_value | (((byte_ptr[byte_idx] >> bit_idx) & 1) << i);
                    ++bit_idx;
                    byte_idx += bit_idx / 8;
                    bit_idx %= 8;
                }
                change_flag = change_flag || (new_value ^ pins_value[*p]);
                pins_value[*p] = new_value;
            } else {
                int new_value = (byte_ptr[byte_idx] >> bit_idx) & 1;
                change_flag = change_flag || (new_value ^ pins_value[*p]);  // 用逻辑运算短路提高效率

                pins_value[*p] = new_value;     // 更新信号值

                ++bit_idx;
                byte_idx += bit_idx / 8;
                bit_idx %= 8;
            }
        }
    }
    return change_flag;
}

// 需要自己在模块中实现
// 即调用所有需要的update_signal
// 已重构
#if 0
int update_output_signals();
#endif

// 转化为json输出
// 只输出output类型
void print_pins_map() {
    // 因为json不允许多余','
    // 第一行单独处理
    printf("{\"L00\":%u", pins_value[L00]);
    for (int i = L01;i <= L19;++i) {
        printf(",\"%s\":%u", pins_name[i], pins_value[i]);  // 注意前缀','
    }
    for (int i = DP00;i <= DP05;i++) {
        printf(",\"%s\":", pins_name[i]);  // 注意前缀','
        unsigned int value = pins_value[i];
        for (int bit_idx = 7; bit_idx >= 0; bit_idx--) {
            printf("%d", (value >> bit_idx) & 1);
        }

    }
    for (int i = OUTPUT00;i <= OUTPUT03;i++) {
        char hex_str[OUTPUT_HEX_MAX_BITS / BITS_PER_HEX + 1];
        sprintf(hex_str, "%08x", pins_value[i]);
        printf(",\"%s\":\"%s\"", pins_name[i], hex_str);
    }
    printf("}\n");
}

int wait_for_input_and_update_input_signals() {
    nlohmann::json input_json;
    try {
        if (std::cin >> input_json) {
            update_input_signals_from_json(input_json);
        }
    }
    catch (const std::exception &e) {
        if (std::cin.eof()) { // 检测 EOF
            return 1;
        } else {
            std::cerr << "JSON Parse Error: " << e.what() << std::endl;
        }
    }
    return 0;
}

// 对update_out_put_map()和print_pins_map()的包装
// 如果有改变 输出对应信号值
// 没有改变 输出空行
// 自动flush
void update_and_print() {
    if (update_output_signals()) {
        print_pins_map();
    } else {
        printf("\n");
    }
    fflush(stdout);
}

#endif