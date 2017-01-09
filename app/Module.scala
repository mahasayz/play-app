import java.time.Clock

import com.google.inject.AbstractModule

/**
  * Created by malam on 9/6/16.
  */
class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[Init]).asEagerSingleton()

  }

}
