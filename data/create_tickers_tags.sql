-- -------------------------------------------------------------
-- TablePlus 4.5.2(402)
--
-- https://tableplus.com/
--
-- Database: baochuan
-- Generation Time: 2022-01-07 12:10:24.3370
-- -------------------------------------------------------------


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


CREATE TABLE `tickers_tags` (
  `ticker_id` int(11) NOT NULL,
  `tag_id` int(11) NOT NULL,
  PRIMARY KEY (`ticker_id`,`tag_id`),
  KEY `fk_tag_id` (`tag_id`),
  CONSTRAINT `fk_tag_id` FOREIGN KEY (`tag_id`) REFERENCES `tags` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_ticker_id` FOREIGN KEY (`ticker_id`) REFERENCES `tickers` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `tickers_tags` (`ticker_id`, `tag_id`) VALUES
(1, 1),
(2, 1),
(3, 1),
(4, 1),
(5, 1),
(5, 2),
(6, 1),
(7, 1),
(8, 1),
(9, 3),
(9, 4),
(9, 5),
(10, 5),
(11, 2),
(11, 3),
(11, 4),
(12, 3),
(12, 5),
(13, 3),
(13, 5),
(14, 5),
(15, 2),
(15, 3),
(15, 4),
(16, 4),
(17, 4),
(18, 2),
(18, 3),
(18, 4),
(19, 5),
(20, 2),
(20, 4),
(21, 5),
(22, 5),
(23, 3),
(23, 4),
(24, 6),
(25, 6),
(26, 6),
(27, 6),
(28, 6),
(29, 6),
(30, 1),
(30, 6),
(31, 6),
(32, 1),
(32, 6),
(33, 6),
(34, 7),
(35, 7),
(36, 7),
(37, 7);


/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;