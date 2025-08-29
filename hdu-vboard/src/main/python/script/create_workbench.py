import os
import json
import argparse


def generate_main_cpp(bind_file, module_name, output_file):
    # 读取 bind.json 文件
    with open(bind_file, "r") as f:
        bind_data = json.load(f)

    pin_bindings = []

    # 对于输入信号 绑定 读取 更新
    for input_entry in bind_data["inputRows"]:
        input_signal = input_entry["signal"]
        input_pins = input_entry["pins"]

        if input_signal != "":
            pin_bindings.append(
                # pin[1]=>pin即可改回原来版本
                f"""input_pins_map.push_back(\n\t\tpin_map {{\n\t\t\t&top->{input_signal}, \n\t\t\t{{{', '.join(f'{pin}' for pin in input_pins)}}},\n\t\t\tsizeof(top->{input_signal})\n\t\t}}\n\t);"""
            )

    # 对于输出信号 绑定 更新
    for output_entry in bind_data["outputRows"]:
        output_signal = output_entry["signal"]
        output_pins = output_entry["pins"]

        if output_signal != "":
            pin_bindings.append(
                f"""output_pins_map.push_back(\n\t\tpin_map {{\n\t\t\t&top->{output_signal}, \n\t\t\t{{{', '.join(f'{pin}' for pin in output_pins)}}},\n\t\t\tsizeof(top->{output_signal})\n\t\t}}\n\t);"""
            )

    # 检验clk信号是否存在
    if bind_data.get("CLK") and bind_data["CLK"] != "":
        input_signal = bind_data["CLK"]
        pin_bindings.append(
            # pin[1]=>pin即可改回原来版本
            f"""input_pins_map.push_back(\n\t\tpin_map {{\n\t\t\t&top->{input_signal}, \n\t\t\t{{{"CLK"}}},\n\t\t\tsizeof(top->{input_signal})\n\t\t}}\n\t);"""
        )

    # 生成 main.cpp 的内容
    main_cpp_content = f"""#include "V{module_name}.h"
#include "verilated.h"
#include <iostream>
#include <mutex>
#include <atomic>
#include <nlohmann/json.hpp>
#include <chrono>
#include "../../../../src/main/cpp/header/vboard.hh"

using namespace std;

V{module_name} *top = new V{module_name};

// 创建一个用于绑定信号和引脚的映射
void bind_all_pins() {{
    {(chr(10)+chr(9)).join(pin_bindings)}
}}

int main(int argc, char **argv) {{
    Verilated::commandArgs(argc, argv);
    vluint64_t main_time = 0;

    bind_all_pins();
    top->eval();
    update_output_signals();
    print_pins_map();
    fflush(stdout);

    while (!Verilated::gotFinish()) {{
        {{        
            if (wait_for_input_and_update_input_signals())
                break;
            
            top->eval();

            update_and_print();
        }}
    }}

    delete top;
    return 0;
}}
"""
    # 将内容写入指定文件
    with open(output_file, "w") as f:
        f.write(main_cpp_content)
    print(f"Generated {output_file} successfully.")

    # 将内容写入指定文件
    with open(output_file, "w") as f:
        f.write(main_cpp_content)
    print(f"Generated {output_file} successfully.")


def create_workspace(
        workspace_name, module_name, verilog_files, bind_file, verilator_path
):
    # 工作区基础路径
    base_dir = os.path.abspath(workspace_name)

    # 定义目录结构
    dirs = [
        base_dir,
        os.path.join(base_dir, "csrc"),
        os.path.join(base_dir, "vsrc"),
        os.path.join(base_dir, "constr"),  # 新增 constr 目录
    ]

    # 创建目录
    for directory in dirs:
        os.makedirs(directory, exist_ok=True)
        print(f"Created directory: {directory}")

    # 复制所有 verilog 文件
    # verilog_file_names = []
    for vfile in verilog_files:
        basename = os.path.basename(vfile)
        dst_path = os.path.join(base_dir, "vsrc", basename)
        with open(dst_path, "wb") as f_dst:
            with open(vfile, "rb") as f_src:
                f_dst.write(f_src.read())
        # verilog_file_names.append(basename)
        print(f"Copied Verilog file to: {dst_path}")

    # 将 bind.json 文件复制到 constr 目录
    constr_path = os.path.join(base_dir, "constr", "bind.json")
    with open(constr_path, "wb") as f:
        with open(bind_file, "rb") as src:
            f.write(src.read())
    print(f"Copied bind.json to: {constr_path}")

    # 生成 main.cpp 文件
    main_cpp_path = os.path.join(base_dir, "csrc", "main.cpp")
    generate_main_cpp(bind_file, module_name, main_cpp_path)

    # 生成 Makefile
    makefile_path = os.path.join(base_dir, "Makefile")
    with open(makefile_path, "w") as f:
        f.write(generate_makefile_content(module_name, verilator_path))
    print(f"Generated Makefile: {makefile_path}")


def generate_makefile_content(topname, verilator_path):
    """
    生成动态 Makefile 内容，TOPNAME 替换为模块名。
    """
    return f"""# 顶层模块名称
TOPNAME = {topname}

# Verilator 编译器配置
VERILATOR = {verilator_path}
VERILATOR_CFLAGS += -MMD --build -cc -O3 --x-assign fast --x-initial fast --noassert

# 构建目录和输出二进制文件
BUILD_DIR = ./build
OBJ_DIR = $(BUILD_DIR)/obj_dir
BIN = $(BUILD_DIR)/$(TOPNAME)

# 默认目标
default: $(BIN)

# 创建构建目录（如果不存在）
$(shell mkdir -p $(BUILD_DIR))

# 搜索项目的 Verilog 和 main.cpp
# 只指定顶层 Verilog 源文件，其余通过 `include` 实现
VTOP_V = $(abspath ./vsrc/$(TOPNAME).v)
CSRCS = $(abspath ./csrc/main.cpp)

# C/C++ 编译器标志
INCFLAGS = $(addprefix -I, $(INC_PATH)) -I./vsrc
CXXFLAGS += $(INCFLAGS) -DTOP_NAME="V$(TOPNAME)"

# 生成二进制文件的规则
$(BIN): $(VTOP_V) $(CSRCS) $(NVBOARD_ARCHIVE)
	@rm -rf $(OBJ_DIR)
	$(VERILATOR) $(VERILATOR_CFLAGS) -I./vsrc \
        --top-module $(TOPNAME) $^ \
    	$(addprefix -CFLAGS , $(CXXFLAGS)) \
  		$(addprefix -LDFLAGS , $(LDFLAGS)) \
        --Mdir $(OBJ_DIR) \
        --exe -o $(abspath $(BIN))

# 其他目标
all: default

run: $(BIN)
	@$^

# 清理构建目录
clean:
	rm -rf $(BUILD_DIR)

.PHONY: default all clean run

"""


def main():
    parser = argparse.ArgumentParser(
        description="Create a Verilator workspace and generate main.cpp."
    )
    parser.add_argument(
        "--workspace-name", required=True, help="Name of the workspace to create."
    )
    parser.add_argument(
        "--verilog-files",
        nargs="+",
        required=True,
        help="Path to the Verilog (.v) file.",
    )
    parser.add_argument(
        "--bind-json", required=True, help="Path to the bind.json file."
    )
    parser.add_argument("--verilator-path", required=True, help="Verilator path")
    parser.add_argument(
        "--top-module",
        required=False,
        help='Top module name, deault: "top"',
        default="top",
    )

    args = parser.parse_args()

    try:
        create_workspace(
            args.workspace_name,
            args.top_module,
            args.verilog_files,
            args.bind_json,
            args.verilator_path,
        )
    except Exception as e:
        print(f"Error: {e}")


if __name__ == "__main__":
    main()
