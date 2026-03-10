-- 为用户表添加头像字段
ALTER TABLE `user` ADD COLUMN `avatar` VARCHAR(255) NULL DEFAULT NULL COMMENT '用户头像URL' AFTER `create_time`;

-- 为用户表添加更多字段以完善用户信息
ALTER TABLE `user` 
ADD COLUMN `name` VARCHAR(50) NULL DEFAULT NULL COMMENT '用户姓名' AFTER `avatar`,
ADD COLUMN `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '用户邮箱' AFTER `name`,
ADD COLUMN `phone` VARCHAR(20) NULL DEFAULT NULL COMMENT '用户手机号' AFTER `email`,
ADD COLUMN `gender` TINYINT NULL DEFAULT NULL COMMENT '用户性别（0-未知，1-男，2-女）' AFTER `phone`,
ADD COLUMN `birthday` DATE NULL DEFAULT NULL COMMENT '用户生日' AFTER `gender`,
ADD COLUMN `address` VARCHAR(255) NULL DEFAULT NULL COMMENT '用户地址' AFTER `birthday`;

-- 添加字段后，可以为现有用户设置默认头像（可选）
-- UPDATE `user` SET `avatar` = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png' WHERE `avatar` IS NULL;