import time
fname = '/usr/share/dict/words'

start = time.time()

with file(fname) as f:
    lines = f.read().splitlines()
    linegroups = [lines[i:i+3] for i in range(0, len(lines), 3)]
    nums = [linegroup[2].split()[0] for linegroup in linegroups]

with file('output2.txt', 'w') as f:
    f.write('\n'.join(nums))   

print 'took ',time.time()-start, ' seconds' 