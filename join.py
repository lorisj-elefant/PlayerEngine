import os

def combine_java_files(src_dir, output_file):
    with open(output_file, 'w', encoding='utf-8') as outfile:
        for root, _, files in os.walk(src_dir):
            for file in files:
                    filepath = os.path.join(root, file)
                    with open(filepath, 'r', encoding='utf-8') as infile:
                        outfile.write(f'{filepath}:\n')
                        outfile.write('```\n')
                        outfile.write(infile.read())
                        outfile.write('\n```\n\n')

# Используйте вашу папку src и имя выходного файла
src_directory = 'src'
output_filename = 'combined_file.txt'
combine_java_files(src_directory, output_filename)
