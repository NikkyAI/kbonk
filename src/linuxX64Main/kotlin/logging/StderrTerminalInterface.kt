package logging

import com.github.ajalt.mordant.terminal.ExperimentalTerminalApi
import com.github.ajalt.mordant.terminal.PrintRequest
import com.github.ajalt.mordant.terminal.TerminalInfo
import com.github.ajalt.mordant.terminal.TerminalInterface
import errPrint
import errPrintln

@OptIn(ExperimentalTerminalApi::class)
class StderrTerminalInterface(
    override val info: TerminalInfo
): TerminalInterface {
    override fun completePrintRequest(request: PrintRequest) {
        if (request.trailingLinebreak) {
            if (request.text.isEmpty()) {
//                fprintf(stderr, "\n")
                errPrintln()
            } else {
//                fprintf(stderr, "${request.text}\n")
                errPrintln(request.text)
            }
        } else {
            errPrint(request.text)
//            fprintf(stderr, request.text)
        }
    }
}