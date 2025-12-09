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