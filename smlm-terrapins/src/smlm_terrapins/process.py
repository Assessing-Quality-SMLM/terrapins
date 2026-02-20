import subprocess as sp
from subprocess import CompletedProcess


def _run_command(arguments: list[str], shell=False) -> None | CompletedProcess:
    print("Running: {}".format(arguments))
    try:
        return sp.run(arguments,
                      cwd=None,
                      shell=shell,
                      capture_output=False,
                      stdout=sp.PIPE,
                      stderr=sp.STDOUT)
    except OSError as e:
        print(e)
        return None
    except sp.CalledProcessError as e:
        print(e.output)
        return None


def get_output(result: CompletedProcess) -> str:
    return result.stdout.decode("utf-8")


def is_ok(result: CompletedProcess) -> bool:
    return result.returncode == 0


def run_with_output(arguments: list[str], shell=False) -> bool:
    print("Running: {}".format(arguments))
    try:
        proc = sp.Popen(arguments,
                        cwd=None,
                        shell=shell,
                        stdout=sp.PIPE,
                        # bufsize=1,
                        universal_newlines=True)
        if proc.stdout is not None:
            for stdout_line in proc.stdout:
                print(stdout_line, end='')
            proc.stdout.close()
        return_code = proc.wait()
        return return_code == 0
    except OSError as e:
        print(e)
        return False
    except sp.CalledProcessError as e:
        print(e.output)
        return False


def command_from_string(command_string: str) -> list[str]:
    return command_string.split(" ")


def run_command(command_string: str, shell=False) -> bool:
    return run_with_output(command_from_string(command_string), shell=shell)


def run_command_and_wait_for_output(arguments: list[str], shell=False) -> None | str:
    result = _run_command(arguments, shell=shell)
    if result is None:
        return None
    return get_output(result)