DROP TABLE IF EXISTS `t_vb_use_record`;
CREATE TABLE t_vb_use_record
(
    `id`             int     NOT NULL AUTO_INCREMENT PRIMARY KEY ,

    -- 用户信息
    `user_name`      varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `user_ip`        varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,

    -- 学院 / 部门
    `school_name`    varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,

    -- 实验数据
    duration         INT                                     DEFAULT NULL COMMENT '实验持续时长（秒）',
    file_upload_time INT                                     DEFAULT NULL COMMENT '文件上传耗时（毫秒）',

    -- 实验状态
    status           TINYINT NOT NULL                        DEFAULT '0' COMMENT '实验状态：0-正常结束 1-异常终止',

#     -- 运行信息
#     start_time       TIMESTAMP NOT NULL COMMENT '实验开始时间',
#     end_time         TIMESTAMP COMMENT '实验结束时间',

    -- 基础字段（BaseEntity）
    create_time      datetime                                DEFAULT NULL,
    update_time      datetime                                DEFAULT NULL,
    `is_deleted`     tinyint                                 DEFAULT '0'
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;