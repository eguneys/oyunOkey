package oyun.app
package templating

import oyun.api.Context

trait RequestHelper {

  def currentUrl(implicit ctx: Context) = ctx.req.host + ctx.req.uri
}
