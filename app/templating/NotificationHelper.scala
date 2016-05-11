package oyun.app
package templating

import oyun.user.User

import play.twirl.api.Html

trait NotificationHelper {

  def notifications(user: User): Html = {
    // val notifs = notificationEnv.api get user.id take 2 map { notif =>
    //   views.html.notification.view(notif.id, notif.from)(Html(notif.html))
    // }
    // Html(notifs.foldLeft("")(_ + _.body))
    Html("")
  }

}
