import scalest.admin.slick.SlickModule
import slick.jdbc.H2Profile

package object pet extends SlickModule[H2Profile](H2Profile)
