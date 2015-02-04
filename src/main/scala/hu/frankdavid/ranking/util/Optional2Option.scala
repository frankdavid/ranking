package hu.frankdavid.ranking.util

import java.util.Optional

object Optional2Option {
  implicit def optional2Option[R](optional: Optional[R]): Option[R] = {
    if (optional.isPresent) {
      Some(optional.get)
    } else {
      None
    }
  }
}
