#ifndef _VBOARD_FUNCTIONS__HH_
#define _VBOARD_FUNCTIONS__HH_

#include "pins.hh"
#include "pins_name.hh"
#include "globals.hh"
#include <map>
#include <vector>
#include <string>
#include <nlohmann/json.hpp>
#include <iostream>

// 从json中读取并更新信号
inline void update_input_signals_from_json(const nlohmann::json &input_json) {
    for (auto &pin : input_pins_map) {
        uint8_t *byte_ptr = static_cast<uint8_t *>(pin.ptr);
        int byte_idx = 0;
        int bit_idx = 0;
        for (auto p = pin.pins_vec.begin();p != pin.pins_vec.end();p++) {
            const char *pin_name = pins_name[*p];
            // 不包含该信号 增位
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
            // 超过最大位数，中断
            if (byte_idx >= pin.max_byte) {
                break;
            }
            // INPUT类型单独处理
            if (*p >= INPUT0 && *p <= INPUT3) {
                std::string hex_str = input_json[pin_name];
                unsigned int hex_num;
                if (!hex_str.empty())
                    hex_num = std::stoul(hex_str, nullptr, 16);
                else
                    hex_num = 0;
                // 内循环可能会超 要判断
                for (int i = 0;i < INPUT_HEX_MAX_BITS && byte_idx < pin.max_byte;i++) {
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
                // 开关和按钮类型
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

// 更新变量信号
// 理论上只需要更新输出变量即可
inline unsigned int update_output_signals() {
    unsigned int change_flag = 0;
    for (auto &pin : output_pins_map) {
        uint8_t *byte_ptr = static_cast<uint8_t *>(pin.ptr);
        int byte_idx = 0;
        int bit_idx = 0;
        for (auto p = pin.pins_vec.begin();p != pin.pins_vec.end();p++) {
            if (byte_idx >= pin.max_byte)  break;
            if (*p >= OUTPUT0 && *p <= OUTPUT5) {
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
            } else if (*p >= DP0 && *p <= DP5) {
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
                unsigned int new_value = (byte_ptr[byte_idx] >> bit_idx) & 1;
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


// 转化为json输出
// 只输出output类型
inline void print_pins_map() {
    // LED信号
    // 因为json不允许多余','
    // 第一行单独处理
    printf("{\"L00\":%u", pins_value[L00]);
    for (int i = L01;i <= L19;++i) {
        printf(",\"%s\":%u", pins_name[i], pins_value[i]);  // 注意前缀','
    }
    // DP信号
    for (int i = DP0;i <= DP5;i++) {
        printf(",\"%s\":\"", pins_name[i]);  // 注意前缀','
        unsigned int value = pins_value[i];
        for (int bit_idx = 7; bit_idx >= 0; bit_idx--) {
            printf("%d", (value >> bit_idx) & 1);
        }
        printf("\"");
        // OUTPUT七段管信号
    }
    for (int i = OUTPUT0;i <= OUTPUT5;i++) {
        char hex_str[OUTPUT_HEX_MAX_BITS / BITS_PER_HEX + 1];
        sprintf(hex_str, "%08X", pins_value[i]);
        printf(",\"%s\":\"%s\"", pins_name[i], hex_str);
    }
    printf("}\n");
}

// 从stdin获取json输入
// 并更新信号
inline int wait_for_input_and_update_input_signals() {
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
inline void update_and_print() {
    if (update_output_signals()) {
        print_pins_map();
    } else {
        printf("{}\n");
    }
    fflush(stdout);
}

#endif