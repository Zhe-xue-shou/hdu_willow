/*
 Navicat Premium Data Transfer

 Source Server         : lwt
 Source Server Type    : MySQL
 Source Server Version : 80036 (8.0.36)
 Source Host           : 124.221.61.179:3306
 Source Schema         : hdu-interrupt

 Target Server Type    : MySQL
 Target Server Version : 80036 (8.0.36)
 File Encoding         : 65001

 Date: 13/04/2024 16:59:21
*/

CREATE
    DATABASE IF NOT EXISTS `hdu-interrupt` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE
    `hdu-interrupt`;

SET NAMES utf8mb4;
SET
    FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for foreign_problem_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `foreign_problem_knowledge`;
CREATE TABLE `foreign_problem_knowledge`
(
    `id`           int NOT NULL AUTO_INCREMENT,
    `problem_id`   int DEFAULT NULL,
    `knowledge_id` int DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for foreign_user_chapter
-- ----------------------------
DROP TABLE IF EXISTS `foreign_user_chapter`;
CREATE TABLE `foreign_user_chapter`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `chapter_id`  int      DEFAULT NULL,
    `user_id`     int      DEFAULT NULL,
    `finish_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_chapter_unique` (`chapter_id`, `user_id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for foreign_user_class
-- ----------------------------
DROP TABLE IF EXISTS `foreign_user_class`;
CREATE TABLE `foreign_user_class`
(
    `id`       int NOT NULL AUTO_INCREMENT,
    `user_id`  int DEFAULT NULL,
    `class_id` int DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for foreign_user_paper
-- ----------------------------
DROP TABLE IF EXISTS `foreign_user_paper`;
CREATE TABLE `foreign_user_paper`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `paper_id`    int      DEFAULT NULL,
    `class_id`    int      DEFAULT NULL,
    `user_id`     int      DEFAULT NULL,
    `link`        longtext COLLATE utf8mb4_general_ci,
    `state`       tinyint  DEFAULT '0' COMMENT '是否批改',
    `grade`       double   DEFAULT NULL,
    `is_deleted`  tinyint  DEFAULT NULL COMMENT '是否打回',
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `user_class_paper_unique` (`paper_id`, `class_id`, `user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_cb
-- ----------------------------
DROP TABLE IF EXISTS `t_cb`;
CREATE TABLE `t_cb`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `ip`          varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `status`      tinyint                                 DEFAULT '0',
    `is_reserved` tinyint                                 DEFAULT '0',
    `is_recorded` tinyint                                 DEFAULT '0',
    `long_id`     varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `create_time` datetime                                DEFAULT NULL,
    `update_time` datetime                                DEFAULT NULL,
    `is_deleted`  tinyint                                 DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_cb_use_record
-- ----------------------------
DROP TABLE IF EXISTS `t_cb_use_record`;
CREATE TABLE `t_cb_use_record`
(
    `id`               int NOT NULL AUTO_INCREMENT,
    `cb_id`            varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `cb_ip`            varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `user_name`        varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `user_ip`          varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `school_name`      varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `duration`         int                                     DEFAULT NULL,
    `file_upload_time` int                                     DEFAULT NULL,
    `create_time`      datetime                                DEFAULT NULL,
    `update_time`      datetime                                DEFAULT NULL,
    `is_deleted`       tinyint                                 DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_chapter
-- ----------------------------
DROP TABLE IF EXISTS `t_chapter`;
CREATE TABLE `t_chapter`
(
    `id`             int NOT NULL AUTO_INCREMENT,
    `intro`          blob COMMENT '目的\r\n',
    `process`        blob COMMENT '过程',
    `video_path`     longtext COLLATE utf8mb4_general_ci COMMENT '视频地址',
    `ppt_path`       longtext COLLATE utf8mb4_general_ci COMMENT 'ppt地址\r\n',
    `animate_path`   longtext COLLATE utf8mb4_general_ci COMMENT '动画地址',
    `link_file_path` longtext COLLATE utf8mb4_general_ci COMMENT '附件地址',
    `mark`           int                                     DEFAULT NULL COMMENT '标记\r\n',
    `number`         int                                     DEFAULT NULL COMMENT '章节序号',
    `title`          varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '标题',
    `is_deleted`     tinyint                                 DEFAULT '0',
    `create_time`    datetime                                DEFAULT NULL,
    `update_time`    datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 4
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_class
-- ----------------------------
DROP TABLE IF EXISTS `t_class`;
CREATE TABLE `t_class`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `name`        varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `is_over`     tinyint                                 DEFAULT '0',
    `create_by`   int                                     DEFAULT NULL,
    `is_deleted`  tinyint                                 DEFAULT '0',
    `create_time` datetime                                DEFAULT NULL,
    `update_time` datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_department
-- ----------------------------
DROP TABLE IF EXISTS `t_department`;
CREATE TABLE `t_department`
(
    `id`                int                                     NOT NULL AUTO_INCREMENT,
    `name`              varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
    `father_department` int                          DEFAULT NULL,
    `is_deleted`        tinyint(1) unsigned zerofill DEFAULT '0',
    `create_time`       datetime                     DEFAULT NULL,
    `update_time`       datetime                     DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_knowledge
-- ----------------------------
DROP TABLE IF EXISTS `t_knowledge`;
CREATE TABLE `t_knowledge`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `knowledge`   varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `is_deleted`  tinyint                                 DEFAULT '0',
    `create_time` datetime                                DEFAULT NULL,
    `update_time` datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 7
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_paper
-- ----------------------------
DROP TABLE IF EXISTS `t_paper`;
CREATE TABLE `t_paper`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `title`       varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `link`        longtext COLLATE utf8mb4_general_ci,
    `class_id`    int                                     DEFAULT NULL,
    `create_by`   int                                     DEFAULT NULL,
    `deadline`    date                                    DEFAULT NULL,
    `is_deleted`  tinyint                                 DEFAULT '0',
    `create_time` datetime                                DEFAULT NULL,
    `update_time` datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_problem
-- ----------------------------
DROP TABLE IF EXISTS `t_problem`;
CREATE TABLE `t_problem`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `type`        int                                    DEFAULT NULL,
    `content`     longtext COLLATE utf8mb4_general_ci,
    `choice`      longtext COLLATE utf8mb4_general_ci,
    `answer`      varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `is_deleted`  tinyint                                DEFAULT '0',
    `create_time` datetime                               DEFAULT NULL,
    `update_time` datetime                               DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 303
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_resource
-- ----------------------------
DROP TABLE IF EXISTS `t_resource`;
CREATE TABLE `t_resource`
(
    `id`            int                                                           NOT NULL AUTO_INCREMENT,
    `resource_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
    `update_time`   datetime DEFAULT NULL,
    `create_time`   datetime DEFAULT NULL,
    `is_deleted`    tinyint  DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_id_name` (`id`, `resource_name`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_role
-- ----------------------------
DROP TABLE IF EXISTS `t_role`;
CREATE TABLE `t_role`
(
    `id`                  int NOT NULL AUTO_INCREMENT,
    `name`                varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `privilege_character` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `privilege_level`     tinyint                                 DEFAULT NULL,
    `enable`              tinyint                                 DEFAULT NULL,
    `is_deleted`          tinyint unsigned                        DEFAULT '0',
    `create_time`         datetime                                DEFAULT NULL,
    `update_time`         datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_sys_file
-- ----------------------------
DROP TABLE IF EXISTS `t_sys_file`;
CREATE TABLE `t_sys_file`
(
    `id`            int NOT NULL AUTO_INCREMENT,
    `name`          varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `original_name` varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `type`          varchar(50) COLLATE utf8mb4_general_ci  DEFAULT NULL,
    `create_user`   varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `info`          varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `absolute_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `resource_path` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `create_time`   datetime                                DEFAULT NULL,
    `update_time`   datetime                                DEFAULT NULL,
    `is_deleted`    tinyint                                 DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_user
-- ----------------------------
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user`
(
    `id`              int                                     NOT NULL AUTO_INCREMENT,
    `username`        varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
    `password`        varchar(100) COLLATE utf8mb4_general_ci NOT NULL,
    `real_name`       varchar(100) COLLATE utf8mb4_general_ci          DEFAULT NULL,
    `active_time`     int                                              DEFAULT NULL,
    `role`            int                                     NOT NULL,
    `department`      int                                              DEFAULT NULL,
    `is_deleted`      tinyint(1)                              NOT NULL DEFAULT '0',
    `create_time`     datetime                                         DEFAULT NULL,
    `update_time`     datetime                                         DEFAULT NULL,
    `tot_active_time` BIGINT                                  NOT NULL DEFAULT 0,
    `tot_exp_cnt`     BIGINT                                  NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_username` (`username`, `department`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_user_resource_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_resource_record`;
CREATE TABLE `t_user_resource_record`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `user_id`     int      DEFAULT NULL,
    `resource_id` int      DEFAULT NULL,
    `duration`    bigint   DEFAULT NULL,
    `times`       int      DEFAULT '1',
    `update_time` datetime DEFAULT NULL,
    `create_time` datetime DEFAULT NULL,
    `is_deleted`  tinyint  DEFAULT '0',
    PRIMARY KEY (`id`),
    UNIQUE KEY `unqiue` (`user_id`, `resource_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_user_test_record
-- ----------------------------
DROP TABLE IF EXISTS `t_user_test_record`;
CREATE TABLE `t_user_test_record`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `user_id`     int      DEFAULT NULL,
    `class_id`    int      DEFAULT NULL,
    `score`       double   DEFAULT NULL,
    `problems`    longtext COLLATE utf8mb4_general_ci,
    `choices`     longtext COLLATE utf8mb4_general_ci,
    `create_time` datetime DEFAULT NULL,
    `update_time` datetime DEFAULT NULL,
    `is_deleted`  tinyint  DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for t_visit_record
-- ----------------------------
DROP TABLE IF EXISTS `t_visit_record`;
CREATE TABLE `t_visit_record`
(
    `id`         bigint NOT NULL AUTO_INCREMENT,
    `session_id` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL,
    `visit_time` datetime                                DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_general_ci;

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`
(
    `id`            bigint       NOT NULL AUTO_INCREMENT,
    `branch_id`     bigint       NOT NULL,
    `xid`           varchar(100) NOT NULL,
    `context`       varchar(128) NOT NULL,
    `rollback_info` longblob     NOT NULL,
    `log_status`    int          NOT NULL,
    `log_created`   datetime     NOT NULL,
    `log_modified`  datetime     NOT NULL,
    `ext`           varchar(100) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb3;

SET
    FOREIGN_KEY_CHECKS = 1;
